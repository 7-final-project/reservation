package com.qring.reservation.domain.model.constraint;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReservationStatus {
    CONFIRMED(Status.CONFIRMED),
    CANCELLED(Status.CANCELLED);

    private final String status;

    public static class Status {
        public static final String CONFIRMED = "확정";
        public static final String CANCELLED = "취소";
    }

    public static ReservationStatus fromString(String status) {
        return switch (status) {
            case Status.CONFIRMED -> ReservationStatus.CONFIRMED;
            case Status.CANCELLED -> ReservationStatus.CANCELLED;
            default -> throw new IllegalArgumentException("유효하지 않은 상태입니다.");
        };
    }
}