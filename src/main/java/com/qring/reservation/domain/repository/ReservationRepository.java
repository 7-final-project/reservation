package com.qring.reservation.domain.repository;

import com.qring.reservation.domain.model.ReservationEntity;

import java.util.Optional;

public interface ReservationRepository {

    ReservationEntity save(ReservationEntity reservationEntity);

    Optional<ReservationEntity> findByIdAndDeletedAtIsNull(Long id);

}
