package com.qring.reservation.infrastructure.messaging;

import com.qring.reservation.application.v1.message.KafkaMessageProducerV1;
import com.qring.reservation.infrastructure.messaging.dto.ReservationCreateEventDTOV1;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaMessageProducerImplV1 implements KafkaMessageProducerV1 {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishReservationCreateEvent(ReservationCreateEventDTOV1.Message message) {

        kafkaTemplate.send("reservation-create-event-topic", message);
    }

    public void publishReservationUpdateEvent(ReservationCreateEventDTOV1.Reservation reservation) {

        kafkaTemplate.send("reservation-update-event-topic", reservation);
    }

    public void publishUserSlackEmailSendEvent(String slackEmail) {

        kafkaTemplate.send("userslackemail-send-event-topic", slackEmail);
    }
}
