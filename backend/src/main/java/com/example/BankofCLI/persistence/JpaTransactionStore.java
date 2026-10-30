package com.example.BankofCLI.persistence;

import com.example.BankofCLI.models.Transaction;
import com.example.BankofCLI.repositories.TransactionRepository;
import com.example.service.port.BankData.TransactionData;
import com.example.service.port.TransactionStore;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;

// Connects the service layer's TransactionStore port to the JPA TransactionRepository.
// History rows are saved in the same transaction as the balance change, so they commit or roll back together.
@Component
public class JpaTransactionStore implements TransactionStore {

    private final TransactionRepository transactions;

    public JpaTransactionStore(TransactionRepository transactions) {
        this.transactions = transactions;
    }

    @Override
    public List<TransactionData> findByAccountId(String accountId) {
        return transactions.findByAccountId(accountId).stream().map(JpaTransactionStore::toData).toList();
    }

    @Override
    public void record(String accountId, String type, BigDecimal amount, Instant date, String description) {
        transactions.save(new Transaction(accountId, type, amount.doubleValue(), date, description));
    }

    static TransactionData toData(Transaction entry) {
        return new TransactionData(
                entry.getId(),
                entry.getAccountId(),
                entry.getType(),
                BigDecimal.valueOf(entry.getAmount()).setScale(2, RoundingMode.HALF_UP),
                entry.getDate() == null ? Instant.EPOCH : Instant.parse(entry.getDate()),
                entry.getDescription()
        );
    }
}
