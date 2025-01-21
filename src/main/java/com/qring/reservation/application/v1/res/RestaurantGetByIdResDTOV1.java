package com.qring.reservation.application.v1.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Getter
@NoArgsConstructor
public class RestaurantGetByIdResDTOV1 {

    private Restaurant restaurant;

    @Getter
    @NoArgsConstructor
    public static class Restaurant {

        private Long id;
        private Long categoryId;
        private String regionCode;
        private String name;
        private String tel;
        private String operationStatus; // NOTE : "영업중" 만 취급하고 있음

    }

    public boolean isOperating() {
        return Objects.equals("영업중", restaurant.getOperationStatus());
    }
}
