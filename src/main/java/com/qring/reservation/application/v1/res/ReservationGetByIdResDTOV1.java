package com.qring.reservation.application.v1.res;

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
public class ReservationGetByIdResDTOV1 {

    private Reservation reservation;
    private ReservationInfo reservationInfo;

    public static ReservationGetByIdResDTOV1 of(ReservationEntity reservationEntity) {
        return ReservationGetByIdResDTOV1.builder()
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
        private ReservationStatus status;
        private int headCount;

        public static Reservation from(ReservationEntity reservationEntity) {
            return Reservation.builder()
                    .id(reservationEntity.getId())
                    .userId(reservationEntity.getUserId())
                    .restaurantId(reservationEntity.getRestaurantId())
                    .status(reservationEntity.getStatus())
                    .headCount(reservationEntity.getHeadCount())
                    .build();
        }
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReservationInfo {

        private Long userId;
        private Long restaurantId;
        private String status;

        public static ReservationInfo from(Long userId, Long restaurantId, String status) {
            return ReservationInfo.builder()
                    .userId(userId)
                    .restaurantId(restaurantId)
                    .status(status)
                    .build();
        }
    }
}
