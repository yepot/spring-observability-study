package com.yepot.observability.global.exception;

import com.yepot.observability.global.exception.dto.ExceptionResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ExceptionResponse> handleYbcException(
        ApiException exception,
        HttpServletRequest request
    ) {
        return ResponseEntity.status(exception.getHttpStatusCode())
            .body(ExceptionResponseFactory.from(exception, request.getRequestURI()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ExceptionResponse> handleIllegalArgument(
        IllegalArgumentException exception,
        HttpServletRequest request
    ) {
        return ResponseEntity.badRequest()
            .body(ExceptionResponseFactory.badRequest(exception.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleException(HttpServletRequest request) {
        return ResponseEntity.internalServerError()
            .body(ExceptionResponseFactory.internalServerError(request.getRequestURI()));
    }
}
