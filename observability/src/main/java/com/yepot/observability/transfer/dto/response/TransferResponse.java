package com.yepot.observability.transfer.dto.response;

import com.yepot.observability.transfer.domain.TransferTransaction;

public record TransferResponse(
    Long transferId,
    Long fromAccountId,
    Long toAccountId,
    Long amount,
    String status
) {

    public static TransferResponse from(TransferTransaction transferTransaction) {
        return new TransferResponse(
            transferTransaction.getTransferId(),
            transferTransaction.getFromAccountId(),
            transferTransaction.getToAccountId(),
            transferTransaction.getAmount(),
            transferTransaction.getStatus().name()
        );
    }
}
