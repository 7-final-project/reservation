package com.qring.reservation.infrastructure.messaging.kafka;

import com.qring.reservation.application.messaging.KafkaMessageProducerV2;
import com.qring.reservation.infrastructure.messaging.kafka.dto.CreateReservationMessageDTOV2;
import com.qring.reservation.infrastructure.messaging.kafka.dto.SendUserInfoMessageDTOV1;
import com.qring.reservation.infrastructure.messaging.kafka.dto.UpdateReservationMessageDTOV1;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaMessageProducerImplV2 implements KafkaMessageProducerV2 {

    @Value("${spring.kafka.topic.reservation-create-event}")
    private String reservationCreateEventTopic;

    @Value("${spring.kafka.topic.reservation-update-event}")
    private String reservationUpdateEventTopic;

    @Value("${spring.kafka.topic.userinfo-send-event}")
    private String userInfoSendEventTopic;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publishReservationCreateEvent(String regionConde, CreateReservationMessageDTOV2 message) {

        kafkaTemplate.send(reservationCreateEventTopic, regionConde, message);
    }

    @Override
    public void publishReservationUpdateEvent(UpdateReservationMessageDTOV1 message) {

        kafkaTemplate.send(reservationUpdateEventTopic, message);
    }

    @Override
    public void publishUserInfoSendEvent(SendUserInfoMessageDTOV1 message) {

        kafkaTemplate.send(userInfoSendEventTopic, message);
    }
}
