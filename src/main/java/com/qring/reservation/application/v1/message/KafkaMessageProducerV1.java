package com.qring.reservation.application.v1.message;

import com.qring.reservation.infrastructure.messaging.dto.ReservationCreateEventDTOV1;

public interface KafkaMessageProducerV1 {

    void publishReservationCreateEvent(ReservationCreateEventDTOV1.Message message);

    void publishReservationUpdateEvent(ReservationCreateEventDTOV1.Reservation reservation);

    void publishUserSlackEmailSendEvent(String slackEmail);

}
