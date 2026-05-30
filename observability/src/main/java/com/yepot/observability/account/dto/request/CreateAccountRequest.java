package com.yepot.observability.account.dto.request;

public record CreateAccountRequest(
    String userName,
    Long initialBalance
) {
}
