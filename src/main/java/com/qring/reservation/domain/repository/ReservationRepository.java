package com.qring.reservation.domain.repository;

import com.qring.reservation.domain.model.ReservationEntity;
import com.qring.reservation.domain.model.constraint.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository {

    ReservationEntity save(ReservationEntity reservationEntity);

    void delete(ReservationEntity reservationEntity);

    Optional<ReservationEntity> findByIdAndDeletedAtIsNull(Long id);

    Page<ReservationEntity> findReservationPageByDeletedAtIsNullWithConditions(Pageable pageable, String userRole, Long userId, Long restaurantId, Long id, String sort);

    Page<ReservationEntity> findReservationPageByDeletedAtIsNullWithOwnerConditions(Pageable pageable, List<Long> restaurantIdListOfOwner, Long userId, Long restaurantId, Long id, String sort);

    boolean existsByUserIdAndRestaurantIdAndStatus(Long userId, Long restaurantId, ReservationStatus status);
}
