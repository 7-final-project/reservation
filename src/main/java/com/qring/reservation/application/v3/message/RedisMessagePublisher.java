package com.qring.reservation.application.v3.message;

public interface RedisMessagePublisher {

    void publish(Long restaurantId, Long reservationId);

}
