package com.qring.reservation.application.v1.service;

import com.qring.reservation.application.v1.res.ReservationPostResDTOV1;
import com.qring.reservation.domain.model.ReservationEntity;
import com.qring.reservation.domain.repository.ReservationRepository;
import com.qring.reservation.presentation.v1.req.PostReservationReqDTOV1;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReservationServiceV1 {

    private final ReservationRepository reservationRepository;

    public ReservationPostResDTOV1 postBy(Long userId, PostReservationReqDTOV1 dto){

        ReservationEntity reservationEntityForSave = ReservationEntity.createReservationEntity(
                userId,
                dto.getReservation().getRestaurantId(),
                dto.getReservation().getHeadCount()
        );

        return ReservationPostResDTOV1.of(reservationRepository.save(reservationEntityForSave));
    }
}