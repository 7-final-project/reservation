package com.qring.reservation.domain.model.constraint;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReservationStatus {
    WAITING(Status.WAITING),
    SEATED(Status.SEATED),
    CANCELLED(Status.CANCELLED);

    private final String status;

    public static class Status {
        public static final String WAITING = "대기";
        public static final String SEATED = "입장";
        public static final String CANCELLED = "취소";
    }

    public static ReservationStatus fromString(String status) {
        return switch (status) {
            case Status.WAITING -> ReservationStatus.WAITING;
            case Status.SEATED -> ReservationStatus.SEATED;
            case Status.CANCELLED -> ReservationStatus.CANCELLED;
            default -> throw new IllegalArgumentException("유효하지 않은 상태입니다.");
        };
    }
}