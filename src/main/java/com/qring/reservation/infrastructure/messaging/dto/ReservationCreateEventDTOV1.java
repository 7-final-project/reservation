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
    private UserInfo userInfo;
    private RestaurantInfo restaurantInfo;
    private ReservationInfo reservationInfo;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {

        private UserInfo userInfo;
        private RestaurantInfo restaurantInfo;
        private ReservationInfo reservationInfo;

        public static Message from(UserInfo userInfo, RestaurantInfo restaurantInfo, ReservationInfo reservationInfo) {
            return Message.builder()
                    .userInfo(userInfo)
                    .restaurantInfo(restaurantInfo)
                    .reservationInfo(reservationInfo)
                    .build();
        }
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {

        private Long userId;
        private String slackEmail;

        public static UserInfo from(Long userId, String slackEmail) {
            return UserInfo.builder()
                    .userId(userId)
                    .slackEmail(slackEmail)
                    .build();
        }
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RestaurantInfo {

        private String restaurantName;
        private String restaurantTel;

        public static RestaurantInfo from(String restaurantName, String restaurantTel) {
            return RestaurantInfo.builder()
                    .restaurantName(restaurantName)
                    .restaurantTel(restaurantTel)
                    .build();
        }
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReservationInfo {

        private Long id;
        private Long restaurantId;
        private int headCount;

        public static ReservationInfo from(Long id, Long restaurantId, int headCount) {
            return ReservationInfo.builder()
                    .id(id)
                    .restaurantId(restaurantId)
                    .headCount(headCount)
                    .build();
        }
    }
}
