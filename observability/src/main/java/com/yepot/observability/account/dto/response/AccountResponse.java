package com.yepot.observability.account.dto.response;

import com.yepot.observability.account.domain.Account;

public record AccountResponse(
    Long accountId,
    String accountNumber,
    String userName,
    Long balance,
    String status
) {

    public static AccountResponse from(Account account) {
        return new AccountResponse(
            account.getAccountId(),
            account.getAccountNumber(),
            account.getUserName(),
            account.getBalance(),
            account.getStatus().name()
        );
    }
}
