package com.yepot.observability.transfer.repository;

import com.yepot.observability.transfer.domain.TransferTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransferTransactionRepository extends JpaRepository<TransferTransaction, Long> {
}
