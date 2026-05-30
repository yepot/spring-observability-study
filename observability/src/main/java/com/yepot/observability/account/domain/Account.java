package com.yepot.observability.account.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long accountId;

    @Column(nullable = false, unique = true)
    private String accountNumber;

    @Column(nullable = false)
    private String userName;

    @Column(nullable = false)
    private Long balance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status;

    protected Account() {
    }

    public Account(String userName, Long balance) {
        this.accountNumber = createPendingAccountNumber();
        this.userName = userName;
        this.balance = balance;
        this.status = AccountStatus.ACTIVE;
    }

    public Long getAccountId() {
        return accountId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getUserName() {
        return userName;
    }

    public Long getBalance() {
        return balance;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void updateAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public void deposit(long amount) {
        this.balance += amount;
    }

    public void withdraw(long amount) {
        this.balance -= amount;
    }

    private String createPendingAccountNumber() {
        return "PENDING-" + UUID.randomUUID();
    }
}
