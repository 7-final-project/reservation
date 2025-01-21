package com.qring.reservation.infrastructure.messaging.v2;

import com.qring.reservation.application.v2.message.KafkaMessageProducerV2;
import com.qring.reservation.infrastructure.messaging.v2.dto.CreateReservationMessageDTOV2;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaMessageProducerImplV2 implements KafkaMessageProducerV2 {

    // -----
    // NOTE : 다중 파티션을 적용한 서비스입니다. 지역 코드를 key 로 가집니다.
    // -----

    @Value("${spring.kafka.topic.reservation-create-event.v2}")
    private String reservationCreateEventTopic;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishReservationCreateEvent(String regionConde, CreateReservationMessageDTOV2 message) {

        kafkaTemplate.send(reservationCreateEventTopic, regionConde, message);
    }
}
