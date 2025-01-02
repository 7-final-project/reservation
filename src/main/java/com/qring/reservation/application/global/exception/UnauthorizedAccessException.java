package com.qring.reservation.application.global.exception;

import lombok.Getter;

@Getter
public class UnauthorizedAccessException extends ReservationException {
    public UnauthorizedAccessException(String message) {
        super(ErrorCode.AUTHORITY_ERROR, message);
    }
}
