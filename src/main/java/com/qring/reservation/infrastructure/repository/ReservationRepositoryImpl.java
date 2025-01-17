package com.qring.reservation.infrastructure.repository;

import com.qring.reservation.application.global.exception.BadRequestException;
import com.qring.reservation.domain.model.QReservationEntity;
import com.qring.reservation.domain.model.ReservationEntity;
import com.qring.reservation.domain.model.constraint.ReservationStatus;
import com.qring.reservation.domain.repository.ReservationRepository;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReservationRepositoryImpl implements ReservationRepository {

    private final JpaReservationRepository jpaReservationRepository;
    private final JPAQueryFactory jpaQueryFactory;

    public ReservationEntity save(ReservationEntity reservationEntity) {
        return jpaReservationRepository.save(reservationEntity);
    }

    public void delete(ReservationEntity reservationEntity) {
        jpaReservationRepository.delete(reservationEntity);
    }

    public Optional<ReservationEntity> findByIdAndDeletedAtIsNull(Long id) {
        return jpaReservationRepository.findByIdAndDeletedAtIsNull(id);
    }

    public boolean existsByUserIdAndRestaurantIdAndStatus(Long userId, Long restaurantId, ReservationStatus status) {
        return jpaReservationRepository.existsByUserIdAndRestaurantIdAndStatus(userId, restaurantId, status);
    }

    // QueryDSL 동적 쿼리
    public Page<ReservationEntity> findReservationPageByDeletedAtIsNullWithConditions(Pageable pageable, String userRole, Long userId, Long restaurantId, Long id, String sort) {

        QReservationEntity reservationEntity = QReservationEntity.reservationEntity;

        // 권한 기반 조건 생성
        BooleanExpression roleCondition = getRoleCondition(userRole, userId);

        // Query 실행
        List<ReservationEntity> results = jpaQueryFactory
                .selectFrom(reservationEntity)
                .where(
                        roleCondition,
                        reservationEntity.deletedAt.isNull(),
                        idEq(id, reservationEntity),
                        userIdEq(userId, reservationEntity),
                        restaurantIdEq(restaurantId, reservationEntity)
                )
                .orderBy(getOrderSpecifier(sort, reservationEntity))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPQLQuery<Long> countQuery = jpaQueryFactory
                .select(reservationEntity.count())
                .from(reservationEntity)
                .where(
                        roleCondition,
                        reservationEntity.deletedAt.isNull(),
                        idEq(id, reservationEntity),
                        userIdEq(userId, reservationEntity),
                        restaurantIdEq(restaurantId, reservationEntity)
                );

        return PageableExecutionUtils.getPage(results, pageable, countQuery::fetchOne);
    }

    public Page<ReservationEntity> findReservationPageByDeletedAtIsNullWithOwnerConditions(Pageable pageable, List<Long> restaurantIdListOfOwner, Long userId, Long restaurantId, Long id, String sort) {

        QReservationEntity reservationEntity = QReservationEntity.reservationEntity;

        // Query 실행
        List<ReservationEntity> results = jpaQueryFactory
                .selectFrom(reservationEntity)
                .where(
                        restaurantIdIn(restaurantIdListOfOwner, reservationEntity),
                        reservationEntity.deletedAt.isNull(),
                        idEq(id, reservationEntity),
                        userIdEq(userId, reservationEntity),
                        restaurantIdEq(restaurantId, reservationEntity)
                )
                .orderBy(getOrderSpecifier(sort, reservationEntity))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPQLQuery<Long> countQuery = jpaQueryFactory
                .select(reservationEntity.count())
                .from(reservationEntity)
                .where(
                        restaurantIdIn(restaurantIdListOfOwner, reservationEntity),
                        reservationEntity.deletedAt.isNull(),
                        idEq(id, reservationEntity),
                        userIdEq(userId, reservationEntity),
                        restaurantIdEq(restaurantId, reservationEntity)
                );

        return PageableExecutionUtils.getPage(results, pageable, countQuery::fetchOne);
    }

    // 권한별 조건 메서드
    private BooleanExpression getRoleCondition(String userRole, Long userId) {
        QReservationEntity reservation = QReservationEntity.reservationEntity;

        switch (userRole) {
            case "관리자":
                return null; // 관리자는 모든 데이터를 조회 가능
            case "고객":
                return reservation.userId.eq(userId); // 고객은 자신의 예약만 조회 가능
            default:
                throw new BadRequestException("유효하지 않은 역할입니다: " + userRole);
        }
    }

    // 동적 쿼리 조건 메서드
    private BooleanExpression idEq(Long id, QReservationEntity reservationEntity) {
        return id != null ? reservationEntity.id.eq(id) : null;
    }

    private BooleanExpression userIdEq(Long userId, QReservationEntity reservationEntity) {
        return userId != null ? reservationEntity.userId.eq(userId) : null;
    }

    private BooleanExpression restaurantIdEq(Long restaurantId, QReservationEntity reservationEntity) {
        return restaurantId != null ? reservationEntity.restaurantId.eq(restaurantId) : null;
    }

    private BooleanExpression restaurantIdIn(List<Long> restaurantIdListOfOwner, QReservationEntity reservationEntity) {
        return restaurantIdListOfOwner != null && !restaurantIdListOfOwner.isEmpty() ? reservationEntity.restaurantId.in(restaurantIdListOfOwner) : null;
    }

    // 정렬 로직
    private OrderSpecifier<?> getOrderSpecifier(String sort, QReservationEntity reservationEntity) {
        if (sort == null) {
            return reservationEntity.createdAt.desc();
        }

        switch (sort) {
            case "SMALLEST":
                return reservationEntity.headCount.asc(); // 인원수 오름차순
            case "LARGEST":
                return reservationEntity.headCount.desc(); // 인원수 내림차순
            case "OLDEST":
                return reservationEntity.createdAt.asc(); // 생성일 오름차순
            default:
                return reservationEntity.createdAt.desc();
        }
    }
}
