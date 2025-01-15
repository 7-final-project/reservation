package com.qring.reservation.application.v1.message;

import com.qring.reservation.infrastructure.messaging.dto.CreateReservationMessageDTOV1;
import com.qring.reservation.infrastructure.messaging.dto.ReservationSendUserInfoEventDTOV1;
import com.qring.reservation.infrastructure.messaging.dto.ReservationUpdateEventDTOV1;

public interface KafkaMessageProducerV1 {

    void publishReservationCreateEvent(CreateReservationMessageDTOV1 message);

    void publishReservationUpdateEvent(ReservationUpdateEventDTOV1.Reservation reservation);

    void publishUserInfoSendEvent(ReservationSendUserInfoEventDTOV1.User user);

}
