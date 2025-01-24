package com.qring.reservation.presentation.req;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PutReservationReqDTOV1 {

    private Reservation reservation;

    @Getter
    public static class Reservation {

        private String status;

    }
}
