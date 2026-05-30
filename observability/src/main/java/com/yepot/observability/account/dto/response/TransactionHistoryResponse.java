package com.yepot.observability.account.dto.response;

import com.yepot.observability.account.domain.AccountTransaction;
import java.time.format.DateTimeFormatter;

public record TransactionHistoryResponse(
    Long transactionId,
    String type,
    Long amount,
    String status,
    String createdAt
) {

    private static final DateTimeFormatter CREATED_AT_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    public static TransactionHistoryResponse from(AccountTransaction transaction) {
        return new TransactionHistoryResponse(
            transaction.getTransactionId(),
            transaction.getTransactionType().name(),
            transaction.getAmount(),
            transaction.getStatus().name(),
            transaction.getCreatedAt().format(CREATED_AT_FORMATTER)
        );
    }
}
