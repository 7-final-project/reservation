package com.qring.reservation.application.v1.service;

import com.qring.reservation.application.global.exception.BadRequestException;
import com.qring.reservation.application.global.exception.EntityNotFoundException;
import com.qring.reservation.application.global.exception.UnauthorizedAccessException;
import com.qring.reservation.application.v1.message.KafkaMessageProducerV1;
import com.qring.reservation.application.v1.res.ReservationGetByIdResDTOV1;
import com.qring.reservation.application.v1.res.ReservationPostResDTOV1;
import com.qring.reservation.application.v1.res.ReservationSearchResDTOV1;
import com.qring.reservation.domain.model.ReservationEntity;
import com.qring.reservation.domain.model.constraint.ReservationStatus;
import com.qring.reservation.domain.repository.ReservationRepository;
import com.qring.reservation.infrastructure.client.RestaurantClient;
import com.qring.reservation.infrastructure.util.PassportUtil;
import com.qring.reservation.presentation.v1.req.PostReservationReqDTOV1;
import com.qring.reservation.presentation.v1.req.PutReservationReqDTOV1;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ReservationServiceV1 {

    private final ReservationRepository reservationRepository;
    private final KafkaMessageProducerV1 kafkaMessageProducerV1;
    private final RestaurantClient restaurantClient;

    @Transactional
    public ReservationPostResDTOV1 postBy(String passport, PostReservationReqDTOV1 dto) {
        // 영업상태 확인
        if (!isRestaurantOpen(dto.getReservation().getRestaurantId())) {
            throw new BadRequestException("현재 영업 중이 아닙니다.");
        }

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

        kafkaMessageProducerV1.publishReservationCreateEvent(reservationInfo);

        return ReservationPostResDTOV1.of(reservationEntityForSave);
    }

    @Transactional(readOnly = true)
    public ReservationGetByIdResDTOV1 getBy(String passport, Long id) {

        ReservationEntity reservationEntityForMapping = getReservationEntityById(id);

        validateAccess(passport, reservationEntityForMapping.getUserId(), reservationEntityForMapping.getRestaurantId());

        return ReservationGetByIdResDTOV1.of(reservationEntityForMapping);
    }

    @Transactional(readOnly = true)
    public ReservationSearchResDTOV1 searchByAdmin(Pageable pageable, String passport, Long userId, Long restaurantId, Long id, String sort) {

        validateUserRole(PassportUtil.getRole(passport), "관리자");

        Page<ReservationEntity> reservationEntityPage = reservationRepository.findReservationPageByDeletedAtIsNullWithConditions(
                pageable,
                "관리자",
                userId,
                restaurantId,
                id,
                sort);

        return ReservationSearchResDTOV1.of(reservationEntityPage);
    }

    @Transactional(readOnly = true)
    public ReservationSearchResDTOV1 searchByCustomer(Pageable pageable, String passport, Long restaurantId, Long id, String sort) {

        validateUserRole(PassportUtil.getRole(passport), "고객");

        Page<ReservationEntity> reservationEntityPage = reservationRepository.findReservationPageByDeletedAtIsNullWithConditions(
                pageable,
                "고객",
                PassportUtil.getUserId(passport),
                restaurantId,
                id,
                sort
        );

        return ReservationSearchResDTOV1.of(reservationEntityPage);
    }

    @Transactional(readOnly = true)
    public ReservationSearchResDTOV1 searchByOwner(Pageable pageable, String passport, Long userId, Long restaurantId, Long id, String sort) {

        validateUserRole(PassportUtil.getRole(passport), "점주");

        // 점주의 소유 식당 목록 - 구현 예정
        Long userIdOfOwner = PassportUtil.getUserId(passport);
        List<Long> restaurantIdListOfOwner = Arrays.asList(1L, 6L);

        Page<ReservationEntity> reservationEntityPage = reservationRepository.findReservationPageByDeletedAtIsNullWithOwnerConditions(
                pageable,
                restaurantIdListOfOwner,
                userId,
                restaurantId,
                id,
                sort
        );

        return ReservationSearchResDTOV1.of(reservationEntityPage);
    }

    @Transactional
    public void putBy(String passport, Long id, PutReservationReqDTOV1 dto) {

        ReservationEntity reservationEntityForModify = getReservationEntityById(id);

        validateAccess(passport, reservationEntityForModify.getUserId(), reservationEntityForModify.getRestaurantId());

        if (reservationEntityForModify.getStatus() == ReservationStatus.CANCELLED) {
            throw new BadRequestException("이미 취소된 예약입니다.");
        } else if (reservationEntityForModify.getStatus() == ReservationStatus.SEATED){
            throw new BadRequestException("이미 입장한 예약입니다.");
        }

        ReservationPostResDTOV1.ReservationInfo reservationInfo = ReservationPostResDTOV1.ReservationInfo.from(
                reservationEntityForModify.getId(),
                reservationEntityForModify.getRestaurantId()
        );

        kafkaMessageProducerV1.publishReservationUpdateEvent(reservationInfo);

        reservationEntityForModify.updateReservationEntityStatus(dto.getReservation().getStatus());
    }

    @Transactional
    public void deleteBy(String passport, Long id) {

        ReservationEntity reservationEntityForDelete = getReservationEntityById(id);

        if (!PassportUtil.getUserId(passport).equals(reservationEntityForDelete.getUserId())) {
            throw new UnauthorizedAccessException("자신의 예약만 접근할 수 있습니다.");
        }

        // 대기 상태 확인 (대기중인 경우 삭제 불가)
        if (reservationEntityForDelete.getStatus() == ReservationStatus.WAITING) {
            throw new BadRequestException("현재 대기중인 예약입니다.");
        }

        reservationEntityForDelete.deleteReservationEntity(PassportUtil.getUsername(passport));
    }

    private void validateAccess(String passport, Long userId, Long restaurantId) {

        String role = PassportUtil.getRole(passport);
        Long userIdOfPassport = PassportUtil.getUserId(passport);
        List<Long> restaurantIdListOfOwner = Arrays.asList(1L, 6L); // 점주의 소유 식당 목록

        switch (role) {
            case "관리자":
                break;
            case "고객":
                if (!Objects.equals(userIdOfPassport, userId)) {
                    throw new UnauthorizedAccessException("자신의 예약만 접근할 수 있습니다.");
                }
                break;
            case "점주":
                if (!restaurantIdListOfOwner.contains(restaurantId)) {
                    throw new UnauthorizedAccessException("자신의 식당 예약만 접근할 수 있습니다.");
                }
                break;
            default:
                throw new BadRequestException("유효하지 않은 역할입니다: " + role);
        }
    }

    public boolean isRestaurantOpen(Long restaurantId) {
        try {
            return "영업중".equals(restaurantClient.getBy(restaurantId).getData().getRestaurant().getOperationStatus());
        } catch (FeignException.NotFound e) {
            throw new EntityNotFoundException("존재하지 않는 식당입니다.");
        } catch (FeignException e) {
            throw new IllegalStateException("식당 서비스 호출 중 문제가 발생했습니다.", e);
        }
    }

    private ReservationEntity getReservationEntityById(Long id) {
        return reservationRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 예약입니다."));
    }

    private void validateUserRole(String role, String requiredRole) {
        if (!role.equals(requiredRole)) {
            throw new UnauthorizedAccessException("접근 권한이 없습니다.");
        }
    }
}