package com.qring.reservation.presentation.v1.controller;

import com.qring.reservation.application.v1.res.ResDTO;
import com.qring.reservation.application.v1.res.ReservationPostResDTOV1;
import com.qring.reservation.domain.model.ReservationEntity;
import com.qring.reservation.presentation.v1.req.PostReservationReqDTOV1;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReservationControllerV1 {

    @PostMapping("/v1/reservations")
    public ResponseEntity<ResDTO<ReservationPostResDTOV1>> postBy(@RequestHeader("X-User-Id") Long userId,
                                                                  @RequestBody PostReservationReqDTOV1 dto) {

        // 더미데이터 ----------------------------------------------
        ReservationEntity dummyReservationEntity = ReservationEntity.builder()
                .userId(1L)
                .restaurantId(501L)
                .headCount(4)
                .build();
        // 추후 삭제 ----------------------------------------------

        return new ResponseEntity<>(
                ResDTO.<ReservationPostResDTOV1>builder()
                        .code(HttpStatus.CREATED.value())
                        .message("예약 생성을 성공했습니다.")
                        .data(ReservationPostResDTOV1.of(dummyReservationEntity))
                        .build(),
                HttpStatus.CREATED
        );
    }
}
