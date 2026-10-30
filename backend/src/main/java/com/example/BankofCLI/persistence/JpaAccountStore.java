package com.example.BankofCLI.persistence;

import com.example.BankofCLI.models.Account;
import com.example.BankofCLI.repositories.AccountRepository;
import com.example.service.contract.BankContracts.AccountType;
import com.example.service.port.AccountStore;
import com.example.service.port.BankData.AccountData;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

// Connects the service layer's AccountStore port to the JPA AccountRepository.
// NOTE: Account stores dollars as a double; the services work in BigDecimal.
// Converting here keeps the entity unchanged for now - switching the column to
// BigDecimal(precision 14, scale 2) is the better long-term fix for money.
@Component
public class JpaAccountStore implements AccountStore {

    private final AccountRepository accounts;

    public JpaAccountStore(AccountRepository accounts) {
        this.accounts = accounts;
    }

    @Override
    public List<AccountData> findByUserId(String userId) {
        return accounts.findByOwnerId(userId).stream().map(JpaAccountStore::toData).toList();
    }

    @Override
    public Optional<AccountData> findOwnedById(String accountId, String userId) {
        return accounts.findByIdAndOwnerId(accountId, userId).map(JpaAccountStore::toData);
    }

    @Override
    public Optional<AccountData> lockOwnedById(String accountId, String userId) {
        return accounts.lockByIdAndOwnerId(accountId, userId).map(JpaAccountStore::toData);
    }

    @Override
    public Optional<AccountData> lockById(String accountId) {
        return accounts.lockById(accountId).map(JpaAccountStore::toData);
    }

    @Override
    public AccountData create(String userId, String name, AccountType type) {
        return toData(accounts.save(new Account(userId, name, type)));
    }

    @Override
    public void updateBalance(String accountId, BigDecimal newBalance) {
        // The row was already loaded and locked in this same transaction,
        // so this returns that managed entity; the change is written when the transaction commits
        Account account = accounts.findById(accountId)
                .orElseThrow(() -> new IllegalStateException("Account " + accountId + " no longer exists."));
        account.setBalance(newBalance.doubleValue());
    }

    static AccountData toData(Account account) {
        return new AccountData(
                account.getId(),
                account.getOwnerId(),
                account.getName(),
                account.getType(),
                BigDecimal.valueOf(account.getBalance()).setScale(2, RoundingMode.HALF_UP),
                account.getCurrency()
        );
    }
}
