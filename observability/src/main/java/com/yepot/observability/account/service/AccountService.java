package com.yepot.observability.account.service;

import com.yepot.observability.account.domain.Account;
import com.yepot.observability.account.dto.request.CreateAccountRequest;
import com.yepot.observability.account.dto.response.AccountResponse;
import com.yepot.observability.account.repository.AccountRepository;
import com.yepot.observability.global.exception.ExceptionCode;
import com.yepot.observability.global.exception.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
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
        Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new ApiException(ExceptionCode.ACCOUNT_NOT_FOUND));

        return AccountResponse.from(account);
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

    private String generateAccountNumber(Long accountId) {
        long normalizedId = accountId % 100_000_000L;
        long prefixBlock = normalizedId / 10_000L;
        long suffixBlock = normalizedId % 10_000L;

        return String.format("100-%04d-%04d", prefixBlock, suffixBlock);
    }
}
