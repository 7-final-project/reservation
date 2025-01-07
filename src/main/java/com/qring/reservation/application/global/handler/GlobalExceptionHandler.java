package com.qring.reservation.application.global.handler;

import com.qring.reservation.application.global.dto.ResDTO;
import com.qring.reservation.application.global.exception.ReservationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({ReservationException.class})
    public ResponseEntity<ResDTO<Object>> ReservationExceptionHandler(ReservationException ex) {
        return new ResponseEntity<>(
                ResDTO.builder()
                        .code(ex.getErrorCode().getCode())
                        .message(ex.getMessage())
                        .build(),
                ex.getErrorCode().getHttpStatus()
        );
    }
}
