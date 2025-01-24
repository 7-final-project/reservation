package com.qring.reservation.infrastructure.client;

import com.qring.reservation.application.global.dto.ResDTO;
import com.qring.reservation.application.res.CouponTableGetByUserIdResDTOV1;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "coupon-service")
public interface CouponClient {

    @GetMapping("/v1/coupons/user-coupons")
    ResponseEntity<ResDTO<CouponTableGetByUserIdResDTOV1>> getBy(@RequestHeader("X-Passport-Token") String passport);

}
