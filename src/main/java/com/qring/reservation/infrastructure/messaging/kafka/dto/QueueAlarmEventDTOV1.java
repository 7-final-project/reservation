package com.qring.reservation.infrastructure.messaging.kafka.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class QueueAlarmEventDTOV1 {
    @JsonProperty("reservationId")
    private Long id;
}
