package com.qring.reservation.application.res;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class RestaurantIdTableResDTOV1 {

    private List<Long> restaurantList;

    public boolean hasRestaurant(Long restaurantId) {
        return restaurantList.contains(restaurantId);
    }
}
