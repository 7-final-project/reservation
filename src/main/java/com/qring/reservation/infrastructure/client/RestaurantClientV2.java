package com.qring.reservation.infrastructure.client;

import com.qring.reservation.application.global.dto.ResDTO;
import com.qring.reservation.application.v2.res.RestaurantGetByIdResDTOV2;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "restaurant-service")
public interface RestaurantClientV2 {

    @GetMapping("/v1/restaurants/{id}")
    ResponseEntity<ResDTO<RestaurantGetByIdResDTOV2>> getBy(@PathVariable(name = "id") Long id);

}
