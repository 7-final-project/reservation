package com.qring.reservation.domain.model;

import com.qring.reservation.domain.model.constraint.ReservationStatus;
import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.*;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "p_reservation")
public class ReservationEntity {

    @Id @Tsid
    @Column(name = "reservation_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "restaurant_Id", nullable = false)
    private Long restaurantId;

    @Column(name = "status", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private ReservationStatus status;

    @Column(name = "head_count", nullable = false)
    private int headCount;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "modified_at" , nullable = false)
    private LocalDateTime modifiedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "created_by", updatable = false)
    private String createdBy;

    @Column(name = "modified_by", nullable = false)
    private String modifiedBy;

    @Column(name = "deleted_by")
    private String deletedBy;

    @Builder
    public ReservationEntity(Long userId, Long restaurantId, int headCount) {
        this.userId = userId;
        this.restaurantId = restaurantId;
        this.status = ReservationStatus.CONFIRMED;
        this.headCount = headCount;
    }
}
