package com.qring.reservation.application.global.exception;

import lombok.Getter;

@Getter
public class ReservationException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String message;

    public ReservationException(ErrorCode errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }

}
