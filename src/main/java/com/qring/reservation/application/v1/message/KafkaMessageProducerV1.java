package com.qring.reservation.application.v1.message;

import com.qring.reservation.application.v1.res.ReservationPostResDTOV1;

public interface KafkaMessageProducerV1 {

    void publishReservationCreateEvent(ReservationPostResDTOV1.ReservationInfo reservationInfo);

}
