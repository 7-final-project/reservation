package com.qring.reservation.application.v2.message;

import com.qring.reservation.infrastructure.messaging.v2.dto.CreateReservationMessageDTOV2;

public interface KafkaMessageProducerV2 {

    void publishReservationCreateEvent(String regionConde, CreateReservationMessageDTOV2 message);

}
