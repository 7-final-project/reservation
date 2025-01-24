package com.qring.reservation.infrastructure.messaging.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qring.reservation.application.global.exception.BadRequestException;
import com.qring.reservation.application.service.ReservationServiceV2;
import com.qring.reservation.infrastructure.messaging.kafka.dto.QueueAlarmEventDTOV1;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaMessageConsumerV1 {

    private final ReservationServiceV2 reservationServiceV2;

    // Kafka 메시지 처리
    @KafkaListener(topics = "${spring.kafka.topic.queue-alarm-event}", groupId = "${spring.kafka.consumer.group-id}")
    public void extractId(String message) {
        try {
            QueueAlarmEventDTOV1 event = parseMessage(message);
            reservationServiceV2.sendUserInfoByEvent(event);
        } catch (Exception e) {
            throw new BadRequestException("메시지 추출 실패: " + message);
        }
    }

    private QueueAlarmEventDTOV1 parseMessage(String message) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(message, QueueAlarmEventDTOV1.class);
        } catch (Exception e) {
            throw new BadRequestException("메시지 추출 실패: " + message);
        }
    }

    @KafkaListener(topics = "${spring.kafka.topic.queue-create-fail-event}", groupId = "${spring.kafka.consumer.group-id}")
    public void handleQueueCreateFailed(String message) {
        try {
            reservationServiceV2.deleteByQueueFailEvent(Long.parseLong(message));
        } catch (Exception e) {
            throw new BadRequestException("메시지 추출 실패: " + message);
        }
    }

    @KafkaListener(topics = "${spring.kafka.topic.queue-delete-fail-event}", groupId = "${spring.kafka.consumer.group-id}")
    public void handleQueueDeleteFailed(String message) {
        try {
            reservationServiceV2.putByQueueFailEvent(Long.parseLong(message));
        } catch (Exception e) {
            throw new BadRequestException("메시지 추출 실패: " + message);
        }
    }
}
