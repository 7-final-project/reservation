package com.qring.reservation.infrastructure.messaging.v1;

import com.qring.reservation.application.v1.message.KafkaMessageProducerV1;
import com.qring.reservation.infrastructure.messaging.v1.dto.CreateReservationMessageDTOV1;
import com.qring.reservation.infrastructure.messaging.v1.dto.SendUserInfoMessageDTOV1;
import com.qring.reservation.infrastructure.messaging.v1.dto.UpdateReservationMessageDTOV1;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaMessageProducerImplV1 implements KafkaMessageProducerV1 {

    // -----
    // NOTE : 단일 파티션을 적용한 서비스입니다.
    // -----

    @Value("${spring.kafka.topic.reservation-create-event.v1}")
    // -----
    /*
        NOTE
          1. 단일 파티션 : v1
          2. 단일 파티션 + 배치처리 : v2
    */
    // -----
    private String reservationCreateEventTopic;

    @Value("${spring.kafka.topic.reservation-update-event}")
    private String reservationUpdateEventTopic;

    @Value("${spring.kafka.topic.userinfo-send-event}")
    private String userInfoSendEventTopic;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishReservationCreateEvent(CreateReservationMessageDTOV1 message) {

        kafkaTemplate.send(reservationCreateEventTopic, message);
    }

    public void publishReservationUpdateEvent(UpdateReservationMessageDTOV1 message) {

        kafkaTemplate.send(reservationUpdateEventTopic, message);
    }

    public void publishUserInfoSendEvent(SendUserInfoMessageDTOV1 message) {

        kafkaTemplate.send(userInfoSendEventTopic, message);
    }
}
