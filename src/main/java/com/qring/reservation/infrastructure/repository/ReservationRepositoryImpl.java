package com.qring.reservation.infrastructure.repository;

import com.qring.reservation.domain.model.ReservationEntity;
import com.qring.reservation.domain.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReservationRepositoryImpl implements ReservationRepository {

    private final JpaReservationRepository jpaReservationRepository;

    public ReservationEntity save(ReservationEntity reservationEntity) {
        return jpaReservationRepository.save(reservationEntity);
    }
}
