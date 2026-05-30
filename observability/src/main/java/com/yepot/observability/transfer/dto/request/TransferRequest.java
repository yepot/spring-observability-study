package com.yepot.observability.transfer.dto.request;

public record TransferRequest(
    Long fromAccountId,
    Long toAccountId,
    Long amount
) {
}
