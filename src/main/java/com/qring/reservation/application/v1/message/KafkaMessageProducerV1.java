package com.qring.reservation.application.v1.message;

import com.qring.reservation.infrastructure.messaging.v1.dto.CreateReservationMessageDTOV1;
import com.qring.reservation.infrastructure.messaging.v1.dto.SendUserInfoMessageDTOV1;
import com.qring.reservation.infrastructure.messaging.v1.dto.UpdateReservationMessageDTOV1;

public interface KafkaMessageProducerV1 {

    void publishReservationCreateEvent(CreateReservationMessageDTOV1 message);

    void publishReservationUpdateEvent(UpdateReservationMessageDTOV1 message);

    void publishUserInfoSendEvent(SendUserInfoMessageDTOV1 message);

}
