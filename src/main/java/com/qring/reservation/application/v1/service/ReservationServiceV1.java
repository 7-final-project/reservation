package com.qring.reservation.application.v1.service;

import com.qring.reservation.application.global.exception.BadRequestException;
import com.qring.reservation.application.global.exception.EntityNotFoundException;
import com.qring.reservation.application.global.exception.UnauthorizedAccessException;
import com.qring.reservation.application.v1.res.ReservationGetByIdResDTOV1;
import com.qring.reservation.application.v1.res.ReservationPostResDTOV1;
import com.qring.reservation.application.v1.res.ReservationSearchResDTOV1;
import com.qring.reservation.domain.model.ReservationEntity;
import com.qring.reservation.domain.model.constraint.ReservationStatus;
import com.qring.reservation.domain.repository.ReservationRepository;
import com.qring.reservation.infrastructure.util.PassportUtil;
import com.qring.reservation.presentation.v1.req.PostReservationReqDTOV1;
import com.qring.reservation.presentation.v1.req.PutReservationReqDTOV1;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ReservationServiceV1 {

    private final ReservationRepository reservationRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public ReservationPostResDTOV1 postBy(String passport, PostReservationReqDTOV1 dto) {

        ReservationEntity reservationEntityForSave = ReservationEntity.createReservationEntity(
                PassportUtil.getUserId(passport),
                dto.getReservation().getRestaurantId(),
                dto.getReservation().getHeadCount()
        );

        reservationRepository.save(reservationEntityForSave);

        ReservationPostResDTOV1.ReservationInfo reservationInfo = ReservationPostResDTOV1.ReservationInfo.from(
                reservationEntityForSave.getId(),
                reservationEntityForSave.getRestaurantId()
        );

        publishReservationCreateEvent(reservationInfo);

        return ReservationPostResDTOV1.of(reservationEntityForSave);
    }

    @Transactional(readOnly = true)
    public ReservationGetByIdResDTOV1 getBy(String passport, Long id) {

        String role = PassportUtil.getRole(passport);
        Long userId = PassportUtil.getUserId(passport);

        ReservationEntity reservationEntityForMapping = getReservationEntityById(id);

        Long restaurantId = 1L;

        // 권한별 처리
        switch (role) {
            case "관리자":
                // 관리자는 모든 예약에 접근 가능
                break;
            case "고객":
                if (!reservationEntityForMapping.getUserId().equals(userId)) {
                    throw new UnauthorizedAccessException("자신의 예약만 조회할 수 있습니다.");
                }
                break;
            case "점주":
                if (!reservationEntityForMapping.getRestaurantId().equals(restaurantId)) {
                    throw new UnauthorizedAccessException("자신의 식당 예약만 조회할 수 있습니다.");
                }
                break;
            default:
                throw new BadRequestException("유효하지 않은 역할입니다: " + role);
        }

        return ReservationGetByIdResDTOV1.of(reservationEntityForMapping);
    }

    @Transactional(readOnly = true)
    public ReservationSearchResDTOV1 searchByAdmin(Pageable pageable, String passport, Long userId, Long restaurantId, Long id, String sort) {

        String role = PassportUtil.getRole(passport);

        validateUserRole(role, "관리자");

        Page<ReservationEntity> reservationEntityPage = reservationRepository.findReservationPageByDeletedAtIsNullWithConditions(pageable, role, userId, restaurantId, id, sort);

        return ReservationSearchResDTOV1.of(reservationEntityPage);
    }

    @Transactional(readOnly = true)
    public ReservationSearchResDTOV1 searchByCustomer(Pageable pageable, String passport, Long restaurantId, Long id, String sort) {

        String role = PassportUtil.getRole(passport);
        Long userId = PassportUtil.getUserId(passport);

        validateUserRole(role, "고객");

        Page<ReservationEntity> reservationEntityPage = reservationRepository.findReservationPageByDeletedAtIsNullWithConditions(pageable, role, userId, restaurantId, id, sort);

        return ReservationSearchResDTOV1.of(reservationEntityPage);
    }

    @Transactional(readOnly = true)
    public ReservationSearchResDTOV1 searchByOwner(Pageable pageable, String passport, Long userId, Long restaurantId, Long id, String sort) {

        String role = PassportUtil.getRole(passport);
        Long userIdOfOwner = PassportUtil.getUserId(passport);

        // 점주의 소유 식당 목록 - 구현 예정
        List<Long> restaurantIdListOfOwner = Arrays.asList(1L, 6L);

        validateUserRole(role, "점주");

        Page<ReservationEntity> reservationEntityPage = reservationRepository.findReservationPageByDeletedAtIsNullWithOwnerConditions(pageable, restaurantIdListOfOwner, userId, restaurantId, id, sort);

        return ReservationSearchResDTOV1.of(reservationEntityPage);
    }

    @Transactional
    public void putBy(String passport, Long id, PutReservationReqDTOV1 dto) {

        String role = PassportUtil.getRole(passport);
        Long userId = PassportUtil.getUserId(passport);

        ReservationEntity reservationEntityForModify = getReservationEntityById(id);

        Long restaurantId = 1L;

        // 예약 상태 확인 (취소 상태인 경우 수정 불가)
        if (reservationEntityForModify.getStatus() == ReservationStatus.CANCELLED) {
            throw new BadRequestException("이미 취소된 예약입니다.");
        }

        // 권한별 처리
        switch (role) {
            case "관리자":
                // 관리자는 모든 예약 상태 변경 가능
                break;
            case "고객":
                if (!reservationEntityForModify.getUserId().equals(userId)) {
                    throw new UnauthorizedAccessException("자신의 예약만 취소할 수 있습니다.");
                }
                break;
            case "점주":
                if (!reservationEntityForModify.getRestaurantId().equals(restaurantId)) {
                    throw new UnauthorizedAccessException("자신의 식당 예약만 취소할 수 있습니다.");
                }
                break;
            default:
                throw new BadRequestException("유효하지 않은 역할입니다: " + role);
        }

        reservationEntityForModify.updateReservationEntityStatus(dto.getReservation().getStatus());
    }

    @Transactional
    public void deleteBy(String passport, Long id) {

        String role = PassportUtil.getRole(passport);
        Long userId = PassportUtil.getUserId(passport);
        String username = PassportUtil.getUsername(passport);

        ReservationEntity reservationEntityForDelete = getReservationEntityById(id);

        Long restaurantId = 1L;

        // 대기 상태 확인 (대기중인 경우 삭제 불가) - 구현 예정

        // 권한별 처리
        switch (role) {
            case "관리자":
                // 관리자는 모든 예약 삭제 가능
                break;
            case "고객":
                if (!reservationEntityForDelete.getUserId().equals(userId)) {
                    throw new UnauthorizedAccessException("자신의 예약만 삭제할 수 있습니다.");
                }
                break;
            case "점주":
                if (!reservationEntityForDelete.getRestaurantId().equals(restaurantId)) {
                    throw new UnauthorizedAccessException("자신의 식당 예약만 삭제할 수 있습니다.");
                }
                break;
            default:
                throw new BadRequestException("유효하지 않은 역할입니다: " + role);
        }

        reservationEntityForDelete.deleteReservationEntity(username);
    }

    private void publishReservationCreateEvent(ReservationPostResDTOV1.ReservationInfo reservationInfo) {

        kafkaTemplate.send("reservation-create-event-topic", reservationInfo);
    }

    private ReservationEntity getReservationEntityById(Long id) {
        return reservationRepository.findByIdAndDeletedAtIsNull(id).orElseThrow(
                () -> new EntityNotFoundException("존재하지 않는 예약입니다.")
        );
    }

    // 권한 검증
    private void validateUserRole(String role, String requiredRole) {
        if (!role.equals(requiredRole)) {
            throw new UnauthorizedAccessException("접근 권한이 없습니다");
        }
    }
}