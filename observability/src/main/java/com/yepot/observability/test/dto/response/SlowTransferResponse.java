package com.yepot.observability.test.dto.response;

public record SlowTransferResponse(
    String message,
    long delayMs
) {
}
