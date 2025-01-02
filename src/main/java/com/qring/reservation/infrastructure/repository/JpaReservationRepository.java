package com.qring.reservation.infrastructure.repository;

import com.qring.reservation.domain.model.ReservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaReservationRepository extends JpaRepository<ReservationEntity, Long> {

}
