package com.qring.reservation.infrastructure.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationUpdateEventDTOV1 {

    private Reservation reservation;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Reservation {

        private Long id;
        private Long restaurantId;

        public static Reservation from(Long id, Long restaurantId) {
            return Reservation.builder()
                    .id(id)
                    .restaurantId(restaurantId)
                    .build();
        }
    }
}
