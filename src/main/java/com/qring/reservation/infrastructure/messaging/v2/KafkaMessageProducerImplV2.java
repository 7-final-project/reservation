package com.qring.reservation.infrastructure.messaging.v2;

import com.qring.reservation.application.v2.message.KafkaMessageProducerV2;
import com.qring.reservation.infrastructure.messaging.v2.dto.CreateReservationMessageDTOV2;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Qualifier("partition-producer")
public class KafkaMessageProducerImplV2 implements KafkaMessageProducerV2 {

    // -----
    // NOTE : 다중 파티션을 적용한 서비스입니다. 지역 코드를 key 로 가집니다.
    // -----

    @Value("${spring.kafka.topic.reservation-create-event.v3}")
    // -----
    /*
        NOTE
          1. 다중 파티션 : v3
          2. 다중 파티션 + 배치처리 : v4
    */
    // -----
    private String reservationCreateEventTopic;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishReservationCreateEvent(String regionConde, CreateReservationMessageDTOV2 message) {

        kafkaTemplate.send(reservationCreateEventTopic, regionConde, message);
    }
}
