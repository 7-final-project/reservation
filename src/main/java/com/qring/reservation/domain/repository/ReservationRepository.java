package com.qring.reservation.domain.repository;

import com.qring.reservation.domain.model.ReservationEntity;

public interface ReservationRepository {

    ReservationEntity save(ReservationEntity reservationEntity);

}
