package com.qring.reservation.application.v1.res;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserGetByIdResDTOV1 {

    private User user;

    @Getter
    @NoArgsConstructor
    public static class User {

        private Long id;
        private String username;
        private String slackEmail;

    }
}
