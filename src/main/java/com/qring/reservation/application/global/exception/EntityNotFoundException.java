package com.qring.reservation.application.global.exception;

import lombok.Getter;

@Getter
public class EntityNotFoundException extends ReservationException {
    public EntityNotFoundException(String message) {
        super(ErrorCode.NOT_FOUND_ERROR, message);
    }
}
