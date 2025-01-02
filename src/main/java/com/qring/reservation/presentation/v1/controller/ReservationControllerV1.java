package com.qring.reservation.presentation.v1.controller;

import com.qring.reservation.application.global.dto.ResDTO;
import com.qring.reservation.application.v1.res.ReservationGetByIdResDTOV1;
import com.qring.reservation.application.v1.res.ReservationPostResDTOV1;
import com.qring.reservation.application.v1.res.ReservationSearchResDTOV1;
import com.qring.reservation.domain.model.ReservationEntity;
import com.qring.reservation.infrastructure.docs.ReservationControllerSwagger;
import com.qring.reservation.presentation.v1.req.PostReservationReqDTOV1;
import com.qring.reservation.presentation.v1.req.PutReservationReqDTOV1;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/reservations")
public class ReservationControllerV1 implements ReservationControllerSwagger {

    @PostMapping
    public ResponseEntity<ResDTO<ReservationPostResDTOV1>> postBy(@RequestHeader("X-User-Id") Long userId,
                                                                  @Valid@RequestBody PostReservationReqDTOV1 dto) {

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
                        .message("예약 생성에 성공했습니다.")
                        .data(ReservationPostResDTOV1.of(dummyReservationEntity))
                        .build(),
                HttpStatus.CREATED
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResDTO<ReservationGetByIdResDTOV1>> getBy(@PathVariable Long id) {

        // 더미데이터 ----------------------------------------------
        ReservationEntity dummyReservationEntity = ReservationEntity.builder()
                .userId(1L)
                .restaurantId(501L)
                .headCount(4)
                .build();
        // 추후 삭제 ----------------------------------------------

        return new ResponseEntity<>(
                ResDTO.<ReservationGetByIdResDTOV1>builder()
                        .code(HttpStatus.OK.value())
                        .message("예약 상세 조회에 성공했습니다.")
                        .data(ReservationGetByIdResDTOV1.of(dummyReservationEntity))
                        .build(),
                HttpStatus.OK
        );
    }

    @GetMapping
    public ResponseEntity<ResDTO<ReservationSearchResDTOV1>> searchBy(@PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                                                                      @RequestParam(name = "userId", required = false) Long userId,
                                                                      @RequestParam(name = "id", required = false) Long id,
                                                                      @RequestParam(name = "restaurantId", required = false) Long restaurantId,
                                                                      @RequestParam(name = "sort", required = false) String sort) {

        // 더미데이터 ----------------------------------------------
        List<ReservationEntity> dummyReservations = List.of(
                ReservationEntity.builder()
                        .userId(1L)
                        .restaurantId(501L)
                        .headCount(4)
                        .build(),
                ReservationEntity.builder()
                        .userId(2L)
                        .restaurantId(501L)
                        .headCount(2)
                        .build(),
                ReservationEntity.builder()
                        .userId(1L)
                        .restaurantId(701L)
                        .headCount(6)
                        .build()
        );

        Page<ReservationEntity> dummyPage = new PageImpl<>(dummyReservations, pageable, dummyReservations.size());
        // 추후 삭제 ----------------------------------------------

        return new ResponseEntity<>(
                ResDTO.<ReservationSearchResDTOV1>builder()
                        .code(HttpStatus.OK.value())
                        .message("예약 검색에 성공했습니다.")
                        .data(ReservationSearchResDTOV1.of(dummyPage))
                        .build(),
                HttpStatus.OK
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResDTO<Object>> putBy(@RequestHeader("X-User-Id") Long userId,
                                                @PathVariable Long id,
                                                @RequestBody PutReservationReqDTOV1 dto) {

        return new ResponseEntity<>(
                ResDTO.builder()
                        .code(HttpStatus.OK.value())
                        .message("예약 상태 변경에 성공했습니다.")
                        .build(),
                HttpStatus.OK
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResDTO<Object>> deleteBy(@RequestHeader("X-User-Id") Long userId,
                                                   @PathVariable Long id) {

        return new ResponseEntity<>(
                ResDTO.builder()
                        .code(HttpStatus.OK.value())
                        .message("예약 삭제에 성공했습니다.")
                        .build(),
                HttpStatus.OK
        );
    }
}
