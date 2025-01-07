package com.qring.reservation.application.v1.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantGetByIdResDTOV1 {

    private Restaurant restaurant;
    private RestaurantInfo restaurantInfo;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Restaurant {

        @JsonProperty("id")
        private Long restaurantId;
        private Long categoryId;
        private String name;
        private String tel;
        private String address;
        private String addressDetails;
        private double ratingAverage;
        private String operationStatus;
        private List<OperatingHour> operatingHourList;

        @Getter
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class OperatingHour {

            @JsonProperty("id")
            private Long OperatingHourId;
            private String dayOfWeek;
            private LocalTime openAt;
            private LocalTime closedAt;
        }
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RestaurantInfo {

        private Long restaurantId;
        private String restaurantName;
        private String restaurantTel;

        public static RestaurantGetByIdResDTOV1.RestaurantInfo from(Long restaurantId, String restaurantName, String restaurantTel) {
            return RestaurantGetByIdResDTOV1.RestaurantInfo.builder()
                    .restaurantId(restaurantId)
                    .restaurantName(restaurantName)
                    .restaurantTel(restaurantTel)
                    .build();
        }
    }
}
