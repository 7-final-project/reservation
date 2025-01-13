package com.qring.reservation.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qring.reservation.application.v1.service.ReservationServiceV1;
import com.qring.reservation.infrastructure.messaging.dto.QueueAlarmEventDTOV1;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaMessageConsumerV1 {

    private final ReservationServiceV1 reservationServiceV1;

    // Kafka 메시지 처리
    @KafkaListener(topics = "${spring.kafka.topic.queue-alarm-event}", groupId = "${spring.kafka.consumer.group-id}")
    public void extractId(String message) {
        try {
            QueueAlarmEventDTOV1 event = parseMessage(message);
            log.info("Parsed event: {}", event);
            reservationServiceV1.sendUserInfoByEvent(event);
        } catch (Exception e) {
            log.error("메시지 추출 실패 : {}", message, e);
        }
    }

    private QueueAlarmEventDTOV1 parseMessage(String message) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(message, QueueAlarmEventDTOV1.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid message format: " + message, e);
        }
    }
}
