package com.qring.reservation.application.v1.res;

import com.qring.reservation.domain.model.ReservationEntity;
import com.qring.reservation.domain.model.constraint.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationSearchResDTOV1 {

    private ReservationPage reservationPage;

    public static ReservationSearchResDTOV1 of(Page<ReservationEntity> reservationEntityPage) {
        return ReservationSearchResDTOV1.builder()
                .reservationPage(ReservationPage.from(reservationEntityPage))
                .build();
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReservationPage {

        private List<Reservation> content;
        private PageDetails page;

        public static ReservationPage from(Page<ReservationEntity> reservationEntityPage) {
            return ReservationPage.builder()
                    .content(Reservation.from(reservationEntityPage.getContent()))
                    .page(PageDetails.from(reservationEntityPage))
                    .build();
        }

        @Getter
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Reservation {

            private Long reservationId;
            private Long userId;
            private Long restaurantId;
            private ReservationStatus status;
            private int headCount;

            public static List<Reservation> from(List<ReservationEntity> reservationEntityList) {
                return reservationEntityList.stream()
                        .map(Reservation::from)
                        .toList();
            }

            public static Reservation from(ReservationEntity reservationEntity) {

                return Reservation.builder()
                        .reservationId(reservationEntity.getId())
                        .userId(reservationEntity.getUserId())
                        .restaurantId(reservationEntity.getRestaurantId())
                        .status(reservationEntity.getStatus())
                        .headCount(reservationEntity.getHeadCount())
                        .build();
            }
        }

        @Getter
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class PageDetails {

            private int size;
            private int number;
            private long totalElements;
            private int totalPages;

            public static PageDetails from(Page<ReservationEntity> reservationEntityPage) {
                return PageDetails.builder()
                        .size(reservationEntityPage.getSize())
                        .number(reservationEntityPage.getNumber())
                        .totalElements(reservationEntityPage.getTotalElements())
                        .totalPages(reservationEntityPage.getTotalPages())
                        .build();
            }
        }
    }
}
