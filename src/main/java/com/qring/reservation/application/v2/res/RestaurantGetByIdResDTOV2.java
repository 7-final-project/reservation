package com.qring.reservation.application.v2.res;


import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Getter
@NoArgsConstructor
public class RestaurantGetByIdResDTOV2 {

    private Restaurant restaurant;

    @Getter
    @NoArgsConstructor
    public static class Restaurant {

        private Long id;
        private Long categoryId;
        private String name;
        private String tel;
        private String regionCode;
        private String operationStatus; // NOTE : "영업중" 만 취급하고 있음

    }

    public boolean isOperating() {
        return Objects.equals("영업중", restaurant.getOperationStatus());
    }
}
