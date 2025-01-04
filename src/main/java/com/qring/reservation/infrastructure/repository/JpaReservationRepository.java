package com.qring.reservation.infrastructure.repository;

import com.qring.reservation.domain.model.ReservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaReservationRepository extends JpaRepository<ReservationEntity, Long> {

    Optional<ReservationEntity> findByIdAndDeletedAtIsNull(Long id);

}
