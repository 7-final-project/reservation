package com.qring.reservation.application.global.exception;

import lombok.Getter;

@Getter
public class BadRequestException extends ReservationException {
    public BadRequestException(String message) {
        super(ErrorCode.BAD_REQUEST_ERROR, message);
    }
}
