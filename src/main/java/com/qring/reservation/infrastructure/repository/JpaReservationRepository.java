package com.qring.reservation.infrastructure.repository;

import com.qring.reservation.domain.model.ReservationEntity;
import com.qring.reservation.domain.model.constraint.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaReservationRepository extends JpaRepository<ReservationEntity, Long> {

    Optional<ReservationEntity> findByIdAndDeletedAtIsNull(Long id);

    boolean existsByUserIdAndRestaurantIdAndStatus(Long userId, Long restaurantId, ReservationStatus status);

}
