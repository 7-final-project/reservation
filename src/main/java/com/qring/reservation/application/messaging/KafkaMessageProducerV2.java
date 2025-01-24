package com.qring.reservation.application.messaging;

import com.qring.reservation.infrastructure.messaging.kafka.dto.CreateReservationMessageDTOV2;
import com.qring.reservation.infrastructure.messaging.kafka.dto.SendUserInfoMessageDTOV1;
import com.qring.reservation.infrastructure.messaging.kafka.dto.UpdateReservationMessageDTOV1;

public interface KafkaMessageProducerV2 {

    void publishReservationCreateEvent(String regionConde, CreateReservationMessageDTOV2 message);

    void publishReservationUpdateEvent(UpdateReservationMessageDTOV1 message);

    void publishUserInfoSendEvent(SendUserInfoMessageDTOV1 message);

}
