package com.qring.reservation.application.v1.service;

import com.qring.reservation.application.global.exception.BadRequestException;
import com.qring.reservation.application.global.exception.UnauthorizedAccessException;
import com.qring.reservation.application.v1.res.ReservationGetByIdResDTOV1;
import com.qring.reservation.application.v1.res.ReservationPostResDTOV1;
import com.qring.reservation.domain.model.ReservationEntity;
import com.qring.reservation.domain.repository.ReservationRepository;
import com.qring.reservation.presentation.v1.req.PostReservationReqDTOV1;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReservationServiceV1 {

    private final ReservationRepository reservationRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
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

    @Transactional(readOnly = true)
    public ReservationGetByIdResDTOV1 getBy(String userRole, Long userId, Long id) {

        ReservationEntity reservationEntityForRead = getReservationEntityById(id);

        Long restaurantId = 1L;

        // 권한별 처리
        switch (userRole) {
            case "ADMIN":
                // 관리자는 모든 예약에 접근 가능
                break;

            case "USER":
                if (!reservationEntityForRead.getUserId().equals(userId)) {
                    throw new UnauthorizedAccessException("자신의 예약만 조회할 수 있습니다.");
                }
                break;

            case "OWNER":
                if (!reservationEntityForRead.getRestaurantId().equals(restaurantId)) {
                    throw new UnauthorizedAccessException("자신의 식당 예약만 조회할 수 있습니다.");
                }
                break;

            default:
                throw new BadRequestException("유효하지 않은 역할입니다: " + userRole);
        }

        return ReservationGetByIdResDTOV1.of(reservationEntityForRead);
    }


    private void publishReservationCreateEvent(Long reservationId) {

        Map<String, Object> event = new HashMap<>();
        event.put("reservationId", reservationId);

        kafkaTemplate.send("reservation-create-event-topic", event);
    }

    private ReservationEntity getReservationEntityById(Long id) {
        return reservationRepository.findByIdAndDeletedAtIsNull(id).orElseThrow(
                () -> new BadRequestException("존재하지 않는 예약입니다.")
        );
    }
}