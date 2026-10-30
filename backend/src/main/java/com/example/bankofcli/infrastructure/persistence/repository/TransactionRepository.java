package com.example.bankofcli.infrastructure.persistence.repository;

import com.example.bankofcli.infrastructure.persistence.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    // History for one account - "accountId" matches Transaction's field name
    List<Transaction> findByAccountId(String accountId);
}
