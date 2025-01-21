package com.qring.reservation.infrastructure.messaging.v1.dto;

import com.qring.reservation.application.v1.res.UserGetByIdResDTOV1;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendUserInfoMessageDTOV1 {

    private User user;

    public static SendUserInfoMessageDTOV1 of(UserGetByIdResDTOV1 dto) {
        return SendUserInfoMessageDTOV1.builder()
                .user(User.from(dto))
                .build();
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class User {

        private Long id;
        private String username;
        private String slackEmail;

        public static User from(UserGetByIdResDTOV1 dto) {
            return User.builder()
                    .id(dto.getUser().getId())
                    .username(dto.getUser().getUsername())
                    .slackEmail(dto.getUser().getSlackEmail())
                    .build();
        }
    }
}
