package com.qring.reservation.infrastructure.messaging;

import com.qring.reservation.application.v1.message.KafkaMessageProducerV1;
import com.qring.reservation.application.v1.res.ReservationPostResDTOV1;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaMessageProducerImplV1 implements KafkaMessageProducerV1 {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishReservationCreateEvent(ReservationPostResDTOV1.ReservationInfo reservationInfo) {

        kafkaTemplate.send("reservation-create-event-topic", reservationInfo);
    }

    public void publishReservationUpdateEvent(ReservationPostResDTOV1.ReservationInfo reservationInfo) {

        kafkaTemplate.send("reservation-update-event-topic", reservationInfo);
    }
}
