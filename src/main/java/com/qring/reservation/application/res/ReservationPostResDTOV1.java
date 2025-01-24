package com.qring.reservation.application.res;

import com.qring.reservation.domain.model.ReservationEntity;
import com.qring.reservation.domain.model.constraint.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationPostResDTOV1 {

    private Reservation reservation;

    public static ReservationPostResDTOV1 of(ReservationEntity reservationEntity) {
        return ReservationPostResDTOV1.builder()
                .reservation(Reservation.from(reservationEntity))
                .build();
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Reservation {

        private Long id;
        private Long userId;
        private Long restaurantId;
        private Long userCouponId;
        private ReservationStatus status;
        private int headCount;

        public static Reservation from(ReservationEntity reservationEntity) {
            return Reservation.builder()
                    .id(reservationEntity.getId())
                    .userId(reservationEntity.getUserId())
                    .restaurantId(reservationEntity.getRestaurantId())
                    .userCouponId(reservationEntity.getUserCouponId())
                    .status(reservationEntity.getStatus())
                    .headCount(reservationEntity.getHeadCount())
                    .build();
        }
    }
}
