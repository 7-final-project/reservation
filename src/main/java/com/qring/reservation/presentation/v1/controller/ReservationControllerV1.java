package com.qring.reservation.presentation.v1.controller;

import com.qring.reservation.application.global.dto.ResDTO;
import com.qring.reservation.application.v1.res.ReservationGetByIdResDTOV1;
import com.qring.reservation.application.v1.res.ReservationPostResDTOV1;
import com.qring.reservation.application.v1.res.ReservationSearchResDTOV1;
import com.qring.reservation.application.v1.service.ReservationServiceV1;
import com.qring.reservation.application.v2.service.ReservationServiceV2;
import com.qring.reservation.infrastructure.docs.ReservationControllerSwagger;
import com.qring.reservation.presentation.v1.req.PostReservationReqDTOV1;
import com.qring.reservation.presentation.v1.req.PutReservationReqDTOV1;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/reservations")
public class ReservationControllerV1  {

    private final ReservationServiceV1 reservationServiceV1;
    private final ReservationServiceV2 reservationServiceV2;

    @PostMapping
    public ResponseEntity<ResDTO<ReservationPostResDTOV1>> postBy(@Valid @RequestBody PostReservationReqDTOV1 dto) {

        return new ResponseEntity<>(
                ResDTO.<ReservationPostResDTOV1>builder()
                        .code(HttpStatus.CREATED.value())
                        .message("예약 생성에 성공했습니다.")
                        .data(reservationServiceV2.postBy(dto))
                        .build(),
                HttpStatus.CREATED
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResDTO<ReservationGetByIdResDTOV1>> getBy(@RequestHeader("X-Passport-Token") String passport,
                                                                    @PathVariable Long id) {

        return new ResponseEntity<>(
                ResDTO.<ReservationGetByIdResDTOV1>builder()
                        .code(HttpStatus.OK.value())
                        .message("예약 상세 조회에 성공했습니다.")
                        .data(reservationServiceV1.getBy(passport, id))
                        .build(),
                HttpStatus.OK
        );
    }

    @GetMapping
    public ResponseEntity<ResDTO<ReservationSearchResDTOV1>> searchBy(@PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                                                                      @RequestHeader("X-Passport-Token") String passport,
                                                                      @RequestParam(name = "userId", required = false) Long userId,
                                                                      @RequestParam(name = "restaurantId", required = false) Long restaurantId,
                                                                      @RequestParam(name = "id", required = false) Long id,
                                                                      @RequestParam(name = "sort", required = false) String sort) {

        return new ResponseEntity<>(
                ResDTO.<ReservationSearchResDTOV1>builder()
                        .code(HttpStatus.OK.value())
                        .message("예약 검색에 성공했습니다.")
                        .data(reservationServiceV1.searchBy(pageable, passport, userId, restaurantId, id, sort))
                        .build(),
                HttpStatus.OK
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResDTO<Object>> putBy(@RequestHeader("X-Passport-Token") String passport,
                                                @PathVariable Long id,
                                                @RequestBody PutReservationReqDTOV1 dto) {

        reservationServiceV1.putBy(passport, id, dto);

        return new ResponseEntity<>(
                ResDTO.builder()
                        .code(HttpStatus.OK.value())
                        .message("예약 상태 변경에 성공했습니다.")
                        .build(),
                HttpStatus.OK
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResDTO<Object>> deleteBy(@RequestHeader("X-Passport-Token") String passport,
                                                   @PathVariable Long id) {

        reservationServiceV1.deleteBy(passport, id);

        return new ResponseEntity<>(
                ResDTO.builder()
                        .code(HttpStatus.OK.value())
                        .message("예약 삭제에 성공했습니다.")
                        .build(),
                HttpStatus.OK
        );
    }
}
