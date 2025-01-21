package com.qring.reservation.infrastructure.messaging.v3;

import com.qring.reservation.application.v2.message.KafkaMessageProducerV2;
import com.qring.reservation.infrastructure.messaging.v2.dto.CreateReservationMessageDTOV2;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Qualifier("topic-producer")
public class KafkaMessageProducerImplV3 implements KafkaMessageProducerV2 {

    // -----
    // NOTE : 다중 토픽을 적용한 서비스입니다. 지역 코드를 suffix 로 가집니다.
    // -----

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaAdmin kafkaAdmin;

    public void publishReservationCreateEvent(String regionCode, CreateReservationMessageDTOV2 message) {

        String topicName = "reservation-create-event-" + regionCode;

        kafkaAdmin.createOrModifyTopics(new NewTopic(topicName, 1, (short) 1));

        kafkaTemplate.send(topicName, regionCode, message);
    }
}
