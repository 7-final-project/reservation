package com.qring.reservation.infrastructure.messaging.v1.dto;

import com.qring.reservation.domain.model.ReservationEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateReservationMessageDTOV1 {

    private Reservation reservation;

    public static UpdateReservationMessageDTOV1 of(ReservationEntity reservationEntity) {
        return UpdateReservationMessageDTOV1.builder()
                .reservation(Reservation.from(reservationEntity))
                .build();
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Reservation {

        private Long id;
        private Restaurant restaurant;

        public static Reservation from(ReservationEntity reservationEntity) {
            return Reservation.builder()
                    .id(reservationEntity.getId())
                    .restaurant(Restaurant.from(reservationEntity.getRestaurantId()))
                    .build();
        }

        @Getter
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Restaurant {

            private Long id;

            public static Restaurant from(Long restaurantId) {
                return Restaurant.builder()
                        .id(restaurantId)
                        .build();
            }
        }
    }
}
