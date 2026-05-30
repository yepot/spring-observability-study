package com.yepot.observability.transfer.service;

import com.yepot.observability.account.domain.Account;
import com.yepot.observability.account.repository.AccountRepository;
import com.yepot.observability.global.exception.ApiException;
import com.yepot.observability.global.exception.ExceptionCode;
import com.yepot.observability.transfer.domain.TransferTransaction;
import com.yepot.observability.transfer.dto.request.TransferRequest;
import com.yepot.observability.transfer.dto.response.TransferResponse;
import com.yepot.observability.transfer.repository.TransferTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransferService {

    private final AccountRepository accountRepository;
    private final TransferTransactionRepository transferTransactionRepository;

    public TransferService(
        AccountRepository accountRepository,
        TransferTransactionRepository transferTransactionRepository
    ) {
        this.accountRepository = accountRepository;
        this.transferTransactionRepository = transferTransactionRepository;
    }

    @Transactional
    public TransferResponse transfer(TransferRequest request) {
        validateTransferRequest(request);

        Account fromAccount = accountRepository.findById(request.fromAccountId())
            .orElseThrow(() -> new ApiException(ExceptionCode.ACCOUNT_NOT_FOUND));
        Account toAccount = accountRepository.findById(request.toAccountId())
            .orElseThrow(() -> new ApiException(ExceptionCode.ACCOUNT_NOT_FOUND));

        if (fromAccount.getBalance() < request.amount()) {
            throw new ApiException(ExceptionCode.TRANSFER_INSUFFICIENT_BALANCE);
        }

        fromAccount.withdraw(request.amount());
        toAccount.deposit(request.amount());

        TransferTransaction transferTransaction = transferTransactionRepository.save(
            new TransferTransaction(fromAccount.getAccountId(), toAccount.getAccountId(), request.amount())
        );

        return TransferResponse.from(transferTransaction);
    }

    private void validateTransferRequest(TransferRequest request) {
        if (request == null) {
            throw new ApiException(ExceptionCode.REQUEST_BODY_REQUIRED);
        }

        if (request.fromAccountId() == null) {
            throw new ApiException(ExceptionCode.FROM_ACCOUNT_ID_REQUIRED);
        }

        if (request.toAccountId() == null) {
            throw new ApiException(ExceptionCode.TO_ACCOUNT_ID_REQUIRED);
        }

        if (request.amount() == null) {
            throw new ApiException(ExceptionCode.TRANSFER_AMOUNT_REQUIRED);
        }

        if (request.amount() <= 0) {
            throw new ApiException(ExceptionCode.TRANSFER_AMOUNT_INVALID);
        }
    }
}
