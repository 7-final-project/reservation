package com.qring.reservation.infrastructure.messaging.v4;

import com.qring.reservation.application.v3.message.RedisMessagePublisher;
import com.qring.reservation.infrastructure.messaging.v4.dto.CreateReservationMessageV4;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisMessagePublisherImpl implements RedisMessagePublisher {

    private final RedisTemplate<String, Object> redisTemplate;

    public void publish(Long restaurantId, Long reservationId) {
        redisTemplate.convertAndSend("reservation-create-event", CreateReservationMessageV4.from(restaurantId, reservationId));
    }
}
