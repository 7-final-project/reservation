package com.qring.reservation.infrastructure.messaging;

import com.qring.reservation.application.v1.message.KafkaMessageProducerV1;
import com.qring.reservation.infrastructure.messaging.dto.CreateReservationMessageDTOV1;
import com.qring.reservation.infrastructure.messaging.dto.ReservationSendUserInfoEventDTOV1;
import com.qring.reservation.infrastructure.messaging.dto.ReservationUpdateEventDTOV1;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaMessageProducerImplV1 implements KafkaMessageProducerV1 {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishReservationCreateEvent(CreateReservationMessageDTOV1 message) {

        kafkaTemplate.send("reservation-create-event-topic", message);
    }

    public void publishReservationUpdateEvent(ReservationUpdateEventDTOV1.Reservation reservation) {

        kafkaTemplate.send("reservation-update-event-topic", reservation);
    }

    public void publishUserInfoSendEvent(ReservationSendUserInfoEventDTOV1.User user) {

        kafkaTemplate.send("userinfo-send-event-topic", user);
    }
}
