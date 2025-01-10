package com.qring.reservation.application.v1.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserGetByIdResDTOV1 {

    private User user;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class User {

        @JsonProperty("id")
        private Long userId;
        private String username;
        private String phone;
        private String slackEmail;
    }
}
