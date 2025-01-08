package com.qring.reservation.infrastructure.messaging.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class QueueAlarmEventDTO {
    @JsonProperty("reservationId")
    private Long id;
}
