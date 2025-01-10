package com.qring.reservation.application.v1.service;

import com.qring.reservation.application.global.exception.BadRequestException;
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
import com.qring.reservation.infrastructure.messaging.dto.QueueAlarmEventDTOV1;
import com.qring.reservation.infrastructure.messaging.dto.ReservationCreateEventDTOV1;
import com.qring.reservation.infrastructure.messaging.dto.ReservationUpdateEventDTOV1;
import com.qring.reservation.infrastructure.util.PassportUtil;
import com.qring.reservation.presentation.v1.req.PostReservationReqDTOV1;
import com.qring.reservation.presentation.v1.req.PutReservationReqDTOV1;
import feign.FeignException;
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

        RestaurantGetByIdResDTOV1 restaurant = getRestaurantData(dto.getReservation().getRestaurantId());

        // 영업상태 확인
        if (!"영업중".equals(restaurant.getRestaurant().getOperationStatus())) {
            throw new BadRequestException("현재 영업 중이 아닙니다.");
        }

        // 중복 예약 확인
        boolean isExistsReservation = reservationRepository.existsByUserIdAndRestaurantIdAndStatus(
                PassportUtil.getUserId(passport),
                restaurant.getRestaurant().getRestaurantId(),
                ReservationStatus.WAITING
        );

        if (isExistsReservation) {
            throw new BadRequestException("이미 해당 매장에서 대기 중인 예약이 있습니다.");
        }

        Long userCouponId = dto.getReservation().getUserCouponId();

        if (userCouponId != null) {
            isExistsUserCoupon(passport, userCouponId);
        }

        ReservationEntity reservationEntityForSave = ReservationEntity.createReservationEntity(
                PassportUtil.getUserId(passport),
                dto.getReservation().getRestaurantId(),
                dto.getReservation().getUserCouponId(),
                dto.getReservation().getHeadCount()
        );

        reservationRepository.save(reservationEntityForSave);

        ReservationCreateEventDTOV1.User user = ReservationCreateEventDTOV1.User.from(
                reservationEntityForSave.getUserId(),
                PassportUtil.getSlackEmail(passport),
                PassportUtil.getUsername(passport)
        );

        ReservationCreateEventDTOV1.Restaurant reservationRestaurant = ReservationCreateEventDTOV1.Restaurant.from(
                restaurant.getRestaurant().getName(),
                restaurant.getRestaurant().getTel()
        );

        ReservationCreateEventDTOV1.Reservation reservation = ReservationCreateEventDTOV1.Reservation.from(
                reservationEntityForSave.getId(),
                reservationEntityForSave.getRestaurantId(),
                reservationEntityForSave.getHeadCount()

        );

        ReservationCreateEventDTOV1.Message message = ReservationCreateEventDTOV1.Message.from(
                user,
                reservationRestaurant,
                reservation
        );

        kafkaMessageProducerV1.publishReservationCreateEvent(message);

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
        UserGetByIdResDTOV1.User user = getUserData(getReservationEntityById(event.getId()).getUserId()).getUser();

        // 유저 정보 생성
        ReservationCreateEventDTOV1.User reservationUser = ReservationCreateEventDTOV1.User.from(
                user.getUserId(),
                user.getSlackEmail(),
                user.getUsername()
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

    private UserGetByIdResDTOV1 getUserData(Long userId) {
        try {
            return authClient.getBy(userId).getBody().getData();
        } catch (FeignException.NotFound e) {
            throw new EntityNotFoundException("존재하지 않는 사용자입니다.");
        } catch (FeignException e) {
            throw new IllegalStateException("유저 서비스 호출 중 문제가 발생했습니다.", e);
        }
    }

    private RestaurantGetByIdResDTOV1 getRestaurantData(Long restaurantId) {
        try {
            return restaurantClient.getBy(restaurantId).getBody().getData();
        } catch (FeignException.NotFound e) {
            throw new EntityNotFoundException("존재하지 않는 식당입니다.");
        } catch (FeignException e) {
            throw new IllegalStateException("식당 서비스 호출 중 문제가 발생했습니다.", e);
        }
    }

    public boolean isExistsUserCoupon(String passport, Long couponId) {
        try {
            // Coupon 서비스 호출
            Set<CouponTableGetByUserIdResDTOV1.UserCoupon> userCouponSet = couponClient
                    .getBy(passport)
                    .getBody()
                    .getData()
                    .getUserCouponSet();

            // 특정 couponId가 있는지 확인
            return userCouponSet.stream()
                    .map(CouponTableGetByUserIdResDTOV1.UserCoupon::getCoupon) // Coupon 객체 추출
                    .anyMatch(coupon -> coupon.getId().equals(couponId)); // couponId와 일치하는지 확인
        } catch (FeignException.NotFound e) {
            throw new EntityNotFoundException("존재하지 않는 쿠폰입니다.");
        } catch (FeignException e) {
            throw new IllegalStateException("쿠폰 서비스 호출 중 문제가 발생했습니다.");
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
}