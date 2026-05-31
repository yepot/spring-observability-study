package com.yepot.observability.global.exception.dto;

public record ExceptionResponse(
    String timestamp,
    int status,
    String error,
    String message,
    String path
) {
}
