package com.qring.reservation.application.v1.service;

import com.qring.reservation.application.global.exception.BadRequestException;
import com.qring.reservation.application.global.exception.DuplicateResourceException;
import com.qring.reservation.application.global.exception.EntityNotFoundException;
import com.qring.reservation.application.global.exception.UnauthorizedAccessException;
import com.qring.reservation.application.v1.message.KafkaMessageProducerV1;
import com.qring.reservation.application.v1.res.*;
import com.qring.reservation.domain.model.ReservationEntity;
import com.qring.reservation.domain.model.constraint.ReservationStatus;
import com.qring.reservation.domain.repository.ReservationRepository;
import com.qring.reservation.infrastructure.client.AuthClient;
import com.qring.reservation.infrastructure.client.CouponClient;
import com.qring.reservation.infrastructure.client.RestaurantClient;
import com.qring.reservation.infrastructure.messaging.dto.CreateReservationMessageDTOV1;
import com.qring.reservation.infrastructure.messaging.dto.QueueAlarmEventDTOV1;
import com.qring.reservation.infrastructure.messaging.dto.ReservationSendUserInfoEventDTOV1;
import com.qring.reservation.infrastructure.messaging.dto.ReservationUpdateEventDTOV1;
import com.qring.reservation.infrastructure.util.PassportUtil;
import com.qring.reservation.presentation.v1.req.PostReservationReqDTOV1;
import com.qring.reservation.presentation.v1.req.PutReservationReqDTOV1;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ReservationServiceV1 {

    private final ReservationRepository reservationRepository;
    private final KafkaMessageProducerV1 kafkaMessageProducerV1;
    private final RestaurantClient restaurantClient;
    private final CouponClient couponClient;
    private final AuthClient authClient;

    @Transactional
    public ReservationPostResDTOV1 postBy(String passport, PostReservationReqDTOV1 dto) {

        RestaurantGetByIdResDTOV1 restaurantData = getRestaurantDataByRestaurantId(dto.getReservation().getRestaurantId());

        // NOTE : 운영시간 검증
        validateIsNotOperating(restaurantData);

        // NOTE : 중복 예약 확인
        validateReservationDuplication(PassportUtil.getUserId(passport), restaurantData.getRestaurant().getRestaurantId());

        // NOTE : 쿠폰 검증
        if (dto.getReservation().getUserCouponId() != null) {
            validateUserCouponAccess(dto.getReservation().getUserCouponId(), passport);
        }

        // NOTE : 예약 생성
        ReservationEntity reservationEntityForSave = ReservationEntity.createReservationEntity(
                PassportUtil.getUserId(passport),
                dto.getReservation().getRestaurantId(),
                dto.getReservation().getUserCouponId(),
                dto.getReservation().getHeadCount()
        );

        reservationRepository.save(reservationEntityForSave);

        kafkaMessageProducerV1.publishReservationCreateEvent(CreateReservationMessageDTOV1.of(passport, reservationEntityForSave, restaurantData));

        return ReservationPostResDTOV1.of(reservationEntityForSave);
    }

    @Transactional(readOnly = true)
    public ReservationGetByIdResDTOV1 getBy(String passport, Long id) {

        ReservationEntity reservationEntityForMapping = getReservationEntityById(id);

        validateAccess(passport, reservationEntityForMapping.getUserId(), reservationEntityForMapping.getRestaurantId());

        return ReservationGetByIdResDTOV1.of(reservationEntityForMapping);
    }

    @Transactional(readOnly = true)
    public ReservationSearchResDTOV1 searchByAdmin(Pageable pageable, String passport, Long userId, Long restaurantId, Long id, String sort) {

        validateUserRole(PassportUtil.getRole(passport), "관리자");

        Page<ReservationEntity> reservationEntityPage = reservationRepository.findReservationPageByDeletedAtIsNullWithConditions(
                pageable,
                "관리자",
                userId,
                restaurantId,
                id,
                sort);

        return ReservationSearchResDTOV1.of(reservationEntityPage);
    }

    @Transactional(readOnly = true)
    public ReservationSearchResDTOV1 searchByCustomer(Pageable pageable, String passport, Long restaurantId, Long id, String sort) {

        validateUserRole(PassportUtil.getRole(passport), "고객");

        Page<ReservationEntity> reservationEntityPage = reservationRepository.findReservationPageByDeletedAtIsNullWithConditions(
                pageable,
                "고객",
                PassportUtil.getUserId(passport),
                restaurantId,
                id,
                sort
        );

        return ReservationSearchResDTOV1.of(reservationEntityPage);
    }

    @Transactional(readOnly = true)
    public ReservationSearchResDTOV1 searchByOwner(Pageable pageable, String passport, Long userId, Long restaurantId, Long id, String sort) {

        validateUserRole(PassportUtil.getRole(passport), "점주");

        // 점주의 소유 식당 목록
        List<Long> restaurantIdListOfOwner = getRestaurantIdListByUserId(passport);

        Page<ReservationEntity> reservationEntityPage = reservationRepository.findReservationPageByDeletedAtIsNullWithOwnerConditions(
                pageable,
                restaurantIdListOfOwner,
                userId,
                restaurantId,
                id,
                sort
        );

        return ReservationSearchResDTOV1.of(reservationEntityPage);
    }

    @Transactional
    public void putBy(String passport, Long id, PutReservationReqDTOV1 dto) {

        ReservationEntity reservationEntityForModify = getReservationEntityById(id);

        validateAccess(passport, reservationEntityForModify.getUserId(), reservationEntityForModify.getRestaurantId());

        if (reservationEntityForModify.getStatus() == ReservationStatus.CANCELLED) {
            throw new BadRequestException("이미 취소된 예약입니다.");
        } else if (reservationEntityForModify.getStatus() == ReservationStatus.SEATED){
            throw new BadRequestException("이미 입장한 예약입니다.");
        }

        ReservationUpdateEventDTOV1.Reservation reservation = ReservationUpdateEventDTOV1.Reservation.from(
                reservationEntityForModify.getId(),
                reservationEntityForModify.getRestaurantId()
        );

        kafkaMessageProducerV1.publishReservationUpdateEvent(reservation);

        reservationEntityForModify.updateReservationEntityStatus(dto.getReservation().getStatus());
    }

    @Transactional
    public void deleteBy(String passport, Long id) {

        ReservationEntity reservationEntityForDelete = getReservationEntityById(id);

        if (!PassportUtil.getUserId(passport).equals(reservationEntityForDelete.getUserId())) {
            throw new UnauthorizedAccessException("자신의 예약만 접근할 수 있습니다.");
        }

        // 대기 상태 확인 (대기중인 경우 삭제 불가)
        if (reservationEntityForDelete.getStatus() == ReservationStatus.WAITING) {
            throw new BadRequestException("현재 대기중인 예약입니다.");
        }

        reservationEntityForDelete.deleteReservationEntity(PassportUtil.getUsername(passport));
    }

    public void sendUserInfoByEvent(QueueAlarmEventDTOV1 event) {

        // 유저 정보 조회
        UserGetByIdResDTOV1 dto = Objects.requireNonNull(authClient.getBy(event.getId()).getBody()).getData();

        // 유저 정보 생성
        ReservationSendUserInfoEventDTOV1.User reservationUser = ReservationSendUserInfoEventDTOV1.User.from(
                dto.getUser().getId(),
                dto.getUser().getSlackEmail(),
                dto.getUser().getPhone()
        );

        // Kafka 메시지 발행
        kafkaMessageProducerV1.publishUserInfoSendEvent(reservationUser);
    }

    private void validateAccess(String passport, Long userId, Long restaurantId) {

        String role = PassportUtil.getRole(passport);

        switch (role) {
            case "관리자":
                break;
            case "고객":
                if (!Objects.equals(PassportUtil.getUserId(passport), userId)) {
                    throw new UnauthorizedAccessException("자신의 예약만 접근할 수 있습니다.");
                }
                break;
            case "점주":
                // 점주의 소유 식당 목록
                List<Long> restaurantIdListOfOwner = getRestaurantIdListByUserId(passport);

                if (!restaurantIdListOfOwner.contains(restaurantId)) {
                    throw new UnauthorizedAccessException("자신의 식당 예약만 접근할 수 있습니다.");
                }
                break;
            default:
                throw new BadRequestException("유효하지 않은 역할입니다: " + role);
        }
    }


    private List<Long> getRestaurantIdListByUserId(String passport) {
        return restaurantClient
                .getRestaurantTableByUserId(passport) // Restaurant 서비스 호출
                .getBody()
                .getData()
                .getRestaurantList(); // 식당 ID 리스트 반환
    }

    private ReservationEntity getReservationEntityById(Long id) {
        return reservationRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 예약입니다."));
    }

    private void validateUserRole(String role, String requiredRole) {
        if (!role.equals(requiredRole)) {
            throw new UnauthorizedAccessException("접근 권한이 없습니다.");
        }
    }

    // NOTE : 식당 영업상태 검증
    private void validateIsNotOperating(RestaurantGetByIdResDTOV1 restaurantDataForValidate) {
        if (!restaurantDataForValidate.isOperating()) {
            throw new BadRequestException("영업중인 식당이 아닙니다.");
        }
    }

    // NOTE : 내 쿠폰 검증
    private void validateUserCouponAccess(Long couponId, String passport) {

        CouponTableGetByUserIdResDTOV1 couponDataForValidate = getCouponDataByPassport(passport);

        if (!couponDataForValidate.hasCoupon(couponId)) {
            throw new UnauthorizedAccessException("보유중인 쿠폰이 아닙니다.");
        }
    }

    // NOTE : 식당 조회
    private RestaurantGetByIdResDTOV1 getRestaurantDataByRestaurantId(Long restaurantId) {
        return restaurantClient.getBy(restaurantId).getBody().getData();
    }

    // NOTE : 내 쿠폰 조회
    private CouponTableGetByUserIdResDTOV1 getCouponDataByPassport(String passport) {
        return couponClient.getBy(passport).getBody().getData();
    }

    // NOTE : 중복 예약 검증
    private void validateReservationDuplication(Long userId, Long restaurantId) {
        boolean isExistsReservation = reservationRepository
                .existsByUserIdAndRestaurantIdAndStatus(userId, restaurantId, ReservationStatus.WAITING);

        if (isExistsReservation) {
            throw new DuplicateResourceException("이미 해당 매장에서 대기 중인 예약이 있습니다.");
        }
    }
}