package com.qring.reservation.application.v1.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantIdTableResDTOV1 {

    private List<Long> restaurantList;

}
