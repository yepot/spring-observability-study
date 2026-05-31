package com.yepot.observability.test.dto.request;

public record MassTransferRequest(
    Long fromAccountId,
    Long toAccountId,
    Long amount,
    Integer count
) {
}
