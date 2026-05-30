package com.yepot.observability.global.exception.dto;

public record ExceptionResponse(
    int status,
    String error,
    String message,
    String path,
    String timestamp
) {
}
