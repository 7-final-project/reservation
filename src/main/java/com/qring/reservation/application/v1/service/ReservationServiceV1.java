package com.qring.reservation.application.v1.service;

import com.qring.reservation.application.v1.res.ReservationPostResDTOV1;
import com.qring.reservation.domain.model.ReservationEntity;
import com.qring.reservation.domain.repository.ReservationRepository;
import com.qring.reservation.presentation.v1.req.PostReservationReqDTOV1;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReservationServiceV1 {

    private final ReservationRepository reservationRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public ReservationPostResDTOV1 postBy(Long userId, PostReservationReqDTOV1 dto){

        ReservationEntity reservationEntityForSave = ReservationEntity.createReservationEntity(
                userId,
                dto.getReservation().getRestaurantId(),
                dto.getReservation().getHeadCount()
        );

        reservationRepository.save(reservationEntityForSave);

        publishReservationCreateEvent(reservationEntityForSave.getId());

        return ReservationPostResDTOV1.of(reservationEntityForSave);
    }

    private void publishReservationCreateEvent(Long reservationId) {

        Map<String, Object> event = new HashMap<>();
        event.put("reservationId", reservationId);

        kafkaTemplate.send("reservation-create-event-topic", event);
    }
}