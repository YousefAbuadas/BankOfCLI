package com.example.BankofCLI.repositories;

import com.example.BankofCLI.models.Account;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {

    // Property expressions must match Account's field names exactly: "ownerId", "id"

    // All accounts belonging to one user
    List<Account> findByOwnerId(String ownerId);

    // One account, only if it belongs to this user (stops users reading each other's accounts)
    Optional<Account> findByIdAndOwnerId(String id, String ownerId);

    // Same lookup, but takes a database row lock until the service's transaction ends,
    // so two deposits/transfers on the same account can't overwrite each other's balance
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a where a.id = :id and a.ownerId = :ownerId")
    Optional<Account> lockByIdAndOwnerId(@Param("id") String id, @Param("ownerId") String ownerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a where a.id = :id")
    Optional<Account> lockById(@Param("id") String id);
}
