package com.qring.reservation.application.v1.service;

import com.qring.reservation.application.global.exception.BadRequestException;
import com.qring.reservation.application.global.exception.DuplicateResourceException;
import com.qring.reservation.application.global.exception.EntityNotFoundException;
import com.qring.reservation.application.global.exception.UnauthorizedAccessException;
import com.qring.reservation.application.v1.message.KafkaMessageProducerV1;
import com.qring.reservation.application.v1.res.*;
import com.qring.reservation.domain.model.ReservationEntity;
import com.qring.reservation.domain.model.constraint.ReservationStatus;
import com.qring.reservation.domain.model.constraint.RoleType;
import com.qring.reservation.domain.repository.ReservationRepository;
import com.qring.reservation.infrastructure.client.AuthClient;
import com.qring.reservation.infrastructure.client.CouponClient;
import com.qring.reservation.infrastructure.client.RestaurantClient;
import com.qring.reservation.infrastructure.messaging.dto.CreateReservationMessageDTOV1;
import com.qring.reservation.infrastructure.messaging.dto.QueueAlarmEventDTOV1;
import com.qring.reservation.infrastructure.messaging.dto.SendUserInfoMessageDTOV1;
import com.qring.reservation.infrastructure.messaging.dto.UpdateReservationMessageDTOV1;
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
        //validateReservationDuplication(PassportUtil.getUserId(passport), restaurantData.getRestaurant().getId());

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

        validateUserRole(PassportUtil.getRole(passport), RoleType.ADMIN);

        Page<ReservationEntity> reservationEntityPage = reservationRepository.findReservationPageByDeletedAtIsNullWithConditions(
                pageable,
                RoleType.ADMIN,
                userId,
                restaurantId,
                id,
                sort);

        return ReservationSearchResDTOV1.of(reservationEntityPage);
    }

    @Transactional(readOnly = true)
    public ReservationSearchResDTOV1 searchByCustomer(Pageable pageable, String passport, Long restaurantId, Long id, String sort) {

        validateUserRole(PassportUtil.getRole(passport), RoleType.CUSTOMER);

        Page<ReservationEntity> reservationEntityPage = reservationRepository.findReservationPageByDeletedAtIsNullWithConditions(
                pageable,
                RoleType.CUSTOMER,
                PassportUtil.getUserId(passport),
                restaurantId,
                id,
                sort
        );

        return ReservationSearchResDTOV1.of(reservationEntityPage);
    }

    @Transactional(readOnly = true)
    public ReservationSearchResDTOV1 searchByOwner(Pageable pageable, String passport, Long userId, Long restaurantId, Long id, String sort) {

        validateUserRole(PassportUtil.getRole(passport), RoleType.OWNER);

        // 점주의 소유 식당 목록
        List<Long> restaurantIdListOfOwner = getRestaurantIdListByPassport(passport).getRestaurantList();

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

        validateStatusForUpdate(reservationEntityForModify.getStatus());

        reservationEntityForModify.updateReservationEntityStatus(dto.getReservation().getStatus());

        kafkaMessageProducerV1.publishReservationUpdateEvent(UpdateReservationMessageDTOV1.of(reservationEntityForModify));

    }

    @Transactional
    public void deleteBy(String passport, Long id) {

        ReservationEntity reservationEntityForDelete = getReservationEntityById(id);

        validateUserReservationAccess(passport, reservationEntityForDelete.getUserId());

        validateStatusForDeletion(reservationEntityForDelete.getStatus());

        reservationEntityForDelete.deleteReservationEntity(PassportUtil.getUsername(passport));
    }

    public void sendUserInfoByEvent(QueueAlarmEventDTOV1 event) {

        // 유저 정보 조회
        UserGetByIdResDTOV1 dto = Objects.requireNonNull(authClient.getBy(getReservationEntityById(event.getId()).getUserId()).getBody()).getData();
        
        // Kafka 메시지 발행
        kafkaMessageProducerV1.publishUserInfoSendEvent(SendUserInfoMessageDTOV1.of(dto));
    }

    private void validateAccess(String passport, Long userId, Long restaurantId) {

        String role = PassportUtil.getRole(passport);

        switch (role) {
            case RoleType.ADMIN:
                break;
            case RoleType.CUSTOMER:
                validateUserReservationAccess(passport, userId);
                break;
            case RoleType.OWNER:
                RestaurantIdTableResDTOV1 restaurantData = getRestaurantIdListByPassport(passport);
                validateOwnerReservationAccess(restaurantId, restaurantData);
                break;
            default:
                throw new BadRequestException("유효하지 않은 역할입니다: " + role);
        }
    }

    private RestaurantIdTableResDTOV1 getRestaurantIdListByPassport(String passport) {
        return Objects.requireNonNull(restaurantClient.getRestaurantTableByUserId(passport).getBody()).getData();
    }
    
    private ReservationEntity getReservationEntityById(Long id) {
        return reservationRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 예약입니다."));
    }

    // NOTE : 접근 권한 검증
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

    // NOTE : 예약 상태 검증
    private static void validateReservationStatus(ReservationStatus reservationStatus, Set<ReservationStatus> invalidStatuses, String errorMessage) {
        if (invalidStatuses.contains(reservationStatus)) {
            throw new BadRequestException(errorMessage);
        }
    }
    public void validateStatusForUpdate(ReservationStatus reservationStatus) {
        Set<ReservationStatus> invalidStatusesForUpdate = Set.of(ReservationStatus.CANCELLED, ReservationStatus.SEATED);
        validateReservationStatus(reservationStatus, invalidStatusesForUpdate, "예약 상태가 이미 변경되었습니다.");
    }

    public void validateStatusForDeletion(ReservationStatus reservationStatus) {
        Set<ReservationStatus> invalidStatusesForDeletion = Set.of(ReservationStatus.WAITING);
        validateReservationStatus(reservationStatus, invalidStatusesForDeletion, "현재 대기중인 예약입니다.");
    }

    // NOTE : 본인 예약 검증
    private static void validateUserReservationAccess(String passport, Long userId) {
        if (!PassportUtil.getUserId(passport).equals(userId)) {
            throw new UnauthorizedAccessException("자신의 예약만 접근할 수 있습니다.");
        }
    }

    // NOTE : 본인 식당 예약 검증
    private static void validateOwnerReservationAccess(Long restaurantId, RestaurantIdTableResDTOV1 restaurantData) {
        if (!restaurantData.hasRestaurant(restaurantId)) {
            throw new UnauthorizedAccessException("자신의 식당 예약만 접근할 수 있습니다.");
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