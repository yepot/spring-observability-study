package com.yepot.observability.account.controller;

import com.yepot.observability.account.dto.request.AmountRequest;
import com.yepot.observability.account.dto.response.AccountResponse;
import com.yepot.observability.account.dto.response.AccountTransactionResponse;
import com.yepot.observability.account.service.AccountService;
import com.yepot.observability.account.dto.request.CreateAccountRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/accounts")
    public AccountResponse createAccount(@RequestBody CreateAccountRequest request) {
        return accountService.createAccount(request);
    }

    @GetMapping("/accounts/{accountId}")
    public AccountResponse getAccount(@PathVariable Long accountId) {
        return accountService.getAccount(accountId);
    }

    @PostMapping("/accounts/{accountId}/deposit")
    public AccountTransactionResponse deposit(
        @PathVariable Long accountId,
        @RequestBody AmountRequest request
    ) {
        return accountService.deposit(accountId, request);
    }

    @PostMapping("/accounts/{accountId}/withdraw")
    public AccountTransactionResponse withdraw(
        @PathVariable Long accountId,
        @RequestBody AmountRequest request
    ) {
        return accountService.withdraw(accountId, request);
    }
}
