package com.qring.reservation.infrastructure.docs;

import com.qring.reservation.application.v1.res.ResDTO;
import com.qring.reservation.application.v1.res.ReservationGetByIdResDTOV1;
import com.qring.reservation.application.v1.res.ReservationPostResDTOV1;
import com.qring.reservation.application.v1.res.ReservationSearchResDTOV1;
import com.qring.reservation.presentation.v1.req.PostReservationReqDTOV1;
import com.qring.reservation.presentation.v1.req.PutReservationReqDTOV1;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Reservation", description = "생성, 조회, 검색, 수정, 삭제 관련 예약 API")
public interface ReservationControllerSwagger {

    @Operation(summary = "예약 생성", description = "사용자의 ID 와 식당의 ID 를 기준으로 예약을 생성하는 API 입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "예약 생성 성공", content = @Content(schema = @Schema(implementation = ResDTO.class))),
            @ApiResponse(responseCode = "400", description = "예약 생성 실패.", content = @Content(schema = @Schema(implementation = ResDTO.class)))
    })
    @PostMapping("/v1/reservations")
    ResponseEntity<ResDTO<ReservationPostResDTOV1>> postBy(@RequestHeader("X-User-Id") Long userId, @Valid@RequestBody PostReservationReqDTOV1 dto);


    @Operation(summary = "예약 상세 조회", description = "예약 ID 를 기준으로 예약을 상세 조회하는 API 입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "예약 조회 성공", content = @Content(schema = @Schema(implementation = ResDTO.class))),
            @ApiResponse(responseCode = "400", description = "예약 조회 실패.", content = @Content(schema = @Schema(implementation = ResDTO.class)))
    })
    @GetMapping("/v1/reservations/{reservationId}")
    ResponseEntity<ResDTO<ReservationGetByIdResDTOV1>> getBy(@PathVariable Long reservationId);


    @Operation(summary = "예약 검색", description = "동적 조건을 기준으로 예약을 검색하는 API 입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "예약 검색 성공", content = @Content(schema = @Schema(implementation = ResDTO.class))),
            @ApiResponse(responseCode = "400", description = "예약 검색 실패.", content = @Content(schema = @Schema(implementation = ResDTO.class)))
    })
    @GetMapping("/v1/reservations")
    ResponseEntity<ResDTO<ReservationSearchResDTOV1>> searchBy(@PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                                                               @RequestParam(name = "userId", required = false) Long userId,
                                                               @RequestParam(name = "reservationId", required = false) Long reservationId,
                                                               @RequestParam(name = "restaurantId", required = false) Long restaurantId,
                                                               @RequestParam(name = "sort", required = false) String sort);


    @Operation(summary = "예약 상태 수정", description = "사용자의 ID 와 예약 ID 를 기준으로 예약 상태를 수정하는 API 입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "예약 수정 성공", content = @Content(schema = @Schema(implementation = ResDTO.class))),
            @ApiResponse(responseCode = "400", description = "예약 수정 실패.", content = @Content(schema = @Schema(implementation = ResDTO.class)))
    })
    @PutMapping("/v1/reservations/{reservationId}")
    ResponseEntity<ResDTO<Object>> putBy(@RequestHeader("X-User-Id") Long userId, @PathVariable Long reservationId, @RequestBody PutReservationReqDTOV1 dto);


    @Operation(summary = "예약 삭제", description = "사용자의 ID 와 예약 ID 를 기준으로 예약을 삭제하는 API 입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "예약 삭제 성공", content = @Content(schema = @Schema(implementation = ResDTO.class))),
            @ApiResponse(responseCode = "400", description = "예약 삭제 실패.", content = @Content(schema = @Schema(implementation = ResDTO.class)))
    })
    @DeleteMapping("/v1/reservations/{reservationId}")
    ResponseEntity<ResDTO<Object>> deleteBy(@RequestHeader("X-User-Id") Long userId, @PathVariable Long reservationId);
}
