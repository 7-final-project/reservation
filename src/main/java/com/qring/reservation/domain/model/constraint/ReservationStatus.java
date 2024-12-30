package com.qring.reservation.domain.model.constraint;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReservationStatus {
    CONFIRMED(Status.CONFIRMED), // 확정
    CANCELLED(Status.CANCELLED); // 취소

    private final String status;

    public static class Status {
        public static final String CONFIRMED = "STATUS_CONFIRMED";
        public static final String CANCELLED = "STATUS_CANCELLED";
    }
}