package com.qring.reservation.infrastructure.messaging.v4.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateReservationMessageV4 implements Serializable {

    private Long reservationId;
    private Long restaurantId;

    public static CreateReservationMessageV4 from(Long restaurantId, Long reservationId){
        return CreateReservationMessageV4.builder()
                .reservationId(reservationId)
                .restaurantId(restaurantId)
                .build();
    }

}
