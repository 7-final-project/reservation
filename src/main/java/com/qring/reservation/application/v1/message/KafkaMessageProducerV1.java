package com.qring.reservation.application.v1.message;

import com.qring.reservation.infrastructure.messaging.dto.ReservationCreateEventDTOV1;
import com.qring.reservation.infrastructure.messaging.dto.ReservationUpdateEventDTOV1;

public interface KafkaMessageProducerV1 {

    void publishReservationCreateEvent(ReservationCreateEventDTOV1.Message message);

    void publishReservationUpdateEvent(ReservationUpdateEventDTOV1.Reservation reservation);

    void publishUserInfoSendEvent(ReservationCreateEventDTOV1.User user);

}
