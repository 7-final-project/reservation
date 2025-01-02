package com.qring.reservation.application.global.exception;

import lombok.Getter;

@Getter
public class DuplicateResourceException extends ReservationException {
    public DuplicateResourceException(String message) {
        super(ErrorCode.DUPLICATE_ERROR, message);
    }
}
