package com.qring.reservation.infrastructure.messaging.v2.dto;

import com.qring.reservation.application.v1.res.RestaurantGetByIdResDTOV1;
import com.qring.reservation.application.v2.res.RestaurantGetByIdResDTOV2;
import com.qring.reservation.domain.model.ReservationEntity;
import com.qring.reservation.infrastructure.util.PassportUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateReservationMessageDTOV2 {


    private User user;
    private Reservation reservation;

    public static CreateReservationMessageDTOV2 of(String passport, ReservationEntity reservationEntity, RestaurantGetByIdResDTOV2 restaurantData) {
        return CreateReservationMessageDTOV2.builder()
                .user(User.from(passport))
                .reservation(Reservation.from(reservationEntity, restaurantData))
                .build();
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class User {

        private Long userId;
        private String slackEmail;
        private String username;

        public static User from(String passport) {
            return User.builder()
                    .userId(PassportUtil.getUserId(passport))
                    .slackEmail(PassportUtil.getSlackEmail(passport))
                    .username(PassportUtil.getUsername(passport))
                    .build();
        }
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Reservation {

        private Long id;
        private int headCount;
        private Restaurant restaurant;

        public static Reservation from(ReservationEntity reservationEntity, RestaurantGetByIdResDTOV2 restaurantData) {
            return Reservation.builder()
                    .id(reservationEntity.getId())
                    .headCount(reservationEntity.getHeadCount())
                    .restaurant(Restaurant.from(restaurantData))
                    .build();
        }

        @Getter
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Restaurant {

            private Long id;
            private String name;
            private String tel;

            public static Restaurant from(RestaurantGetByIdResDTOV2 restaurantData) {
                return Restaurant.builder()
                        .id(restaurantData.getRestaurant().getId())
                        .name(restaurantData.getRestaurant().getName())
                        .tel(restaurantData.getRestaurant().getTel())
                        .build();
            }
        }
    }
}
