package com.example.service.port;

import com.example.service.contract.BankContracts.AccountType;
import com.example.service.port.BankData.AccountData;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/** All methods must share the service transaction; do not use REQUIRES_NEW or auto-commit writes. */
public interface AccountStore {
    List<AccountData> findByUserId(String userId);
    Optional<AccountData> findOwnedById(String accountId, String userId);

    /** Obtain a database write lock, held until the enclosing service transaction ends. */
    Optional<AccountData> lockOwnedById(String accountId, String userId);

    /** Lock an account by ID, including accounts owned by another user. */
    Optional<AccountData> lockById(String accountId);

    /** Create a USD account with a zero balance in the current registration transaction. */
    AccountData create(String userId, String name, AccountType type);

    /** Called only after locking the account, in the same transaction. Fail if the row is missing. */
    void updateBalance(String accountId, BigDecimal newBalance);
}
