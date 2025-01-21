package com.qring.reservation.infrastructure.client;

import com.qring.reservation.application.global.dto.ResDTO;
import com.qring.reservation.application.v1.res.RestaurantGetByIdResDTOV1;
import com.qring.reservation.application.v1.res.RestaurantIdTableResDTOV1;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "restaurant-service")
public interface RestaurantClientV1 {

    @GetMapping("/v1/restaurants/{id}")
    ResponseEntity<ResDTO<RestaurantGetByIdResDTOV1>> getBy(@PathVariable(name = "id") Long id);

    @GetMapping("/v1/restaurants/my")
    ResponseEntity<ResDTO<RestaurantIdTableResDTOV1>> getRestaurantTableByUserId(@RequestHeader("X-Passport-Token") String passport);

}
