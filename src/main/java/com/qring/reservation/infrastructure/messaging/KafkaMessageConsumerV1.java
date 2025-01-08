package com.qring.reservation.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qring.reservation.application.v1.service.ReservationServiceV1;
import com.qring.reservation.infrastructure.messaging.dto.QueueAlarmEventDTO;
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
    @KafkaListener(topics = "queue-alarm-event-topic", groupId = "${spring.kafka.consumer.group-id}")
    public void extractId(String message) {
        try {
            QueueAlarmEventDTO event = parseMessage(message);
            log.info("Parsed event: {}", event);
            reservationServiceV1.sendUserSlackEmailByEvent(event);
        } catch (Exception e) {
            log.error("메시지 추출 실패 : {}", message, e);
        }
    }

    private QueueAlarmEventDTO parseMessage(String message) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(message, QueueAlarmEventDTO.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid message format: " + message, e);
        }
    }
}
