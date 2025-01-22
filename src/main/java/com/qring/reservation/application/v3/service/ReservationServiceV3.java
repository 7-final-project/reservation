package com.qring.reservation.application.v3.service;

import com.qring.reservation.application.global.exception.BadRequestException;
import com.qring.reservation.application.global.exception.DuplicateResourceException;
import com.qring.reservation.application.global.exception.UnauthorizedAccessException;
import com.qring.reservation.application.v1.res.CouponTableGetByUserIdResDTOV1;
import com.qring.reservation.application.v1.res.ReservationPostResDTOV1;
import com.qring.reservation.application.v1.res.RestaurantGetByIdResDTOV1;
import com.qring.reservation.application.v3.message.RedisMessagePublisher;
import com.qring.reservation.domain.model.ReservationEntity;
import com.qring.reservation.domain.model.constraint.ReservationStatus;
import com.qring.reservation.domain.repository.ReservationRepository;
import com.qring.reservation.infrastructure.client.CouponClient;
import com.qring.reservation.infrastructure.client.RestaurantClientV1;
import com.qring.reservation.infrastructure.util.PassportUtil;
import com.qring.reservation.presentation.v1.req.PostReservationReqDTOV1;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReservationServiceV3 {

    // -----
    // NOTE : 단일 파티션을 적용한 서비스입니다.
    // -----

    private final ReservationRepository reservationRepository;
    private final RedisMessagePublisher redisMessagePublisher;
    private final RestaurantClientV1 restaurantClientV1;
    private final CouponClient couponClient;

    @Transactional
    public ReservationPostResDTOV1 postBy(String passport, PostReservationReqDTOV1 dto) {

        RestaurantGetByIdResDTOV1 restaurantData = getRestaurantDataByRestaurantId(dto.getReservation().getRestaurantId());

        // NOTE : 운영시간 검증
        validateIsNotOperating(restaurantData);

        // NOTE : 중복 예약 확인
        validateReservationDuplication(PassportUtil.getUserId(passport), restaurantData.getRestaurant().getId());

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

        // 식당 아이디, 예약 아이디
        redisMessagePublisher.publish(reservationEntityForSave.getId(), reservationEntityForSave.getRestaurantId());

        return ReservationPostResDTOV1.of(reservationEntityForSave);
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
        return restaurantClientV1.getByV1(restaurantId).getBody().getData();
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