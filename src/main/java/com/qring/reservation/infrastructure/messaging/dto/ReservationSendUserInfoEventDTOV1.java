package com.qring.reservation.infrastructure.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationSendUserInfoEventDTOV1 {

    private User user;

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
}
