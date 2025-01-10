package com.qring.reservation.infrastructure.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationCreateEventDTOV1 {

    private Message message;
    private User user;
    private Restaurant restaurant;
    private Reservation reservation;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {

        private User user;
        private Restaurant restaurant;
        private Reservation reservation;

        public static Message from(User user, Restaurant restaurant, Reservation reservation) {
            return Message.builder()
                    .user(user)
                    .restaurant(restaurant)
                    .reservation(reservation)
                    .build();
        }
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class User {

        private Long userId;
        private String slackEmail;
        private String username;


        public static User from(Long userId, String slackEmail, String username) {
            return User.builder()
                    .userId(userId)
                    .slackEmail(slackEmail)
                    .username(username)
                    .build();
        }
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Restaurant {

        private String restaurantName;
        private String restaurantTel;

        public static Restaurant from(String restaurantName, String restaurantTel) {
            return Restaurant.builder()
                    .restaurantName(restaurantName)
                    .restaurantTel(restaurantTel)
                    .build();
        }
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Reservation {

        private Long id;
        private Long restaurantId;
        private int headCount;

        public static Reservation from(Long id, Long restaurantId, int headCount) {
            return Reservation.builder()
                    .id(id)
                    .restaurantId(restaurantId)
                    .headCount(headCount)
                    .build();
        }
    }
}
