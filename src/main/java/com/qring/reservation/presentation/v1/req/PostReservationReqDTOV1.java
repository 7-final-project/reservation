package com.qring.reservation.presentation.v1.req;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PostReservationReqDTOV1 {

    @Valid
    @NotNull(message = "예약 정보를 입력해주세요.")
    private Reservation reservation;

    @Getter
    public static class Reservation {

        @NotNull(message = "식당 정보를 입력해주세요.")
        private Long restaurantId;

        @Positive(message = "인원 수를 입력해주세요.")
        @Min(value = 1, message = "예약 인원은 최소 1인입니다.")
        @Max(value = 6, message = "예약 인원은 최대 6인입니다.")
        private int headCount;
    }
}
