package com.leo.vegas.test.wallet.repository;

import com.leo.vegas.test.wallet.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {
    Page<Transaction> findByPlayerAccountAccountId(String id, Pageable pageable);
}
