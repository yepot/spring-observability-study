package com.yepot.observability.account.repository;

import com.yepot.observability.account.domain.AccountTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountTransactionRepository extends JpaRepository<AccountTransaction, Long> {
}
