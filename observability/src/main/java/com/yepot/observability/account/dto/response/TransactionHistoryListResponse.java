package com.yepot.observability.account.dto.response;

import java.util.List;

public record TransactionHistoryListResponse(
    Long accountId,
    List<TransactionHistoryResponse> transactions
) {
}
