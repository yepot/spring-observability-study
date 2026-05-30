package com.yepot.observability.account.dto.response;

import com.yepot.observability.account.domain.Account;
import com.yepot.observability.account.domain.AccountTransaction;

public record AccountTransactionResponse(
    Long accountId,
    Long balance,
    Long transactionId,
    String status
) {

    public static AccountTransactionResponse from(Account account, AccountTransaction transaction) {
        return new AccountTransactionResponse(
            account.getAccountId(),
            account.getBalance(),
            transaction.getTransactionId(),
            transaction.getStatus().name()
        );
    }
}
