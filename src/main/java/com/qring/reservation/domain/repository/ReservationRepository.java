package com.qring.reservation.domain.repository;

import com.qring.reservation.domain.model.ReservationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ReservationRepository {

    ReservationEntity save(ReservationEntity reservationEntity);

    Optional<ReservationEntity> findByIdAndDeletedAtIsNull(Long id);

    Page<ReservationEntity> findReservationPageByDeletedAtIsNullWithConditions(Pageable pageable, String userRole, Long userId, Long restaurantId, Long id, String sort);

}
