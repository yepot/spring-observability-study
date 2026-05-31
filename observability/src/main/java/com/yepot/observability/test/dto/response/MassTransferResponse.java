package com.yepot.observability.test.dto.response;

public record MassTransferResponse(
    int requestedCount,
    int successCount,
    int failCount,
    long totalAmount,
    long durationMs
) {
}
