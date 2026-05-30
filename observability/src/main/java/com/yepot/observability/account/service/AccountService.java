package com.yepot.observability.account.service;

import com.yepot.observability.account.domain.Account;
import com.yepot.observability.account.domain.AccountTransaction;
import com.yepot.observability.account.domain.TransactionType;
import com.yepot.observability.account.dto.request.AmountRequest;
import com.yepot.observability.account.dto.request.CreateAccountRequest;
import com.yepot.observability.account.dto.response.AccountResponse;
import com.yepot.observability.account.dto.response.AccountTransactionResponse;
import com.yepot.observability.account.repository.AccountRepository;
import com.yepot.observability.account.repository.AccountTransactionRepository;
import com.yepot.observability.global.exception.ApiException;
import com.yepot.observability.global.exception.ExceptionCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountTransactionRepository accountTransactionRepository;

    public AccountService(
        AccountRepository accountRepository,
        AccountTransactionRepository accountTransactionRepository
    ) {
        this.accountRepository = accountRepository;
        this.accountTransactionRepository = accountTransactionRepository;
    }

    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {
        validateRequest(request);

        Account account = new Account(request.userName().trim(), request.initialBalance());
        Account savedAccount = accountRepository.save(account);
        savedAccount.updateAccountNumber(generateAccountNumber(savedAccount.getAccountId()));

        return AccountResponse.from(savedAccount);
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccount(Long accountId) {
        Account account = getAccountEntity(accountId);

        return AccountResponse.from(account);
    }

    @Transactional
    public AccountTransactionResponse deposit(Long accountId, AmountRequest request) {
        validateDepositRequest(request);

        Account account = getAccountEntity(accountId);
        account.deposit(request.amount());

        AccountTransaction transaction = accountTransactionRepository.save(
            new AccountTransaction(account.getAccountId(), request.amount(), account.getBalance(), TransactionType.DEPOSIT)
        );

        return AccountTransactionResponse.from(account, transaction);
    }

    @Transactional
    public AccountTransactionResponse withdraw(Long accountId, AmountRequest request) {
        validateWithdrawRequest(request);

        Account account = getAccountEntity(accountId);

        if (account.getBalance() < request.amount()) {
            throw new ApiException(ExceptionCode.INSUFFICIENT_BALANCE);
        }

        account.withdraw(request.amount());

        AccountTransaction transaction = accountTransactionRepository.save(
            new AccountTransaction(account.getAccountId(), request.amount(), account.getBalance(), TransactionType.WITHDRAW)
        );

        return AccountTransactionResponse.from(account, transaction);
    }

    private void validateRequest(CreateAccountRequest request) {
        if (request == null) {
            throw new ApiException(ExceptionCode.REQUEST_BODY_REQUIRED);
        }

        if (request.userName() == null || request.userName().isBlank()) {
            throw new ApiException(ExceptionCode.USER_NAME_REQUIRED);
        }

        if (request.initialBalance() == null) {
            throw new ApiException(ExceptionCode.INITIAL_BALANCE_REQUIRED);
        }

        if (request.initialBalance() < 0) {
            throw new ApiException(ExceptionCode.INITIAL_BALANCE_NEGATIVE);
        }
    }

    private void validateDepositRequest(AmountRequest request) {
        if (request == null || request.amount() == null) {
            throw new ApiException(ExceptionCode.DEPOSIT_AMOUNT_REQUIRED);
        }

        if (request.amount() <= 0) {
            throw new ApiException(ExceptionCode.DEPOSIT_AMOUNT_INVALID);
        }
    }

    private void validateWithdrawRequest(AmountRequest request) {
        if (request == null || request.amount() == null) {
            throw new ApiException(ExceptionCode.WITHDRAW_AMOUNT_REQUIRED);
        }

        if (request.amount() <= 0) {
            throw new ApiException(ExceptionCode.WITHDRAW_AMOUNT_INVALID);
        }
    }

    private Account getAccountEntity(Long accountId) {
        return accountRepository.findById(accountId)
            .orElseThrow(() -> new ApiException(ExceptionCode.ACCOUNT_NOT_FOUND));
    }

    private String generateAccountNumber(Long accountId) {
        long normalizedId = accountId % 100_000_000L;
        long prefixBlock = normalizedId / 10_000L;
        long suffixBlock = normalizedId % 10_000L;

        return String.format("100-%04d-%04d", prefixBlock, suffixBlock);
    }
}
