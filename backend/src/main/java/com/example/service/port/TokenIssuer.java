package com.example.service.port;

import com.example.service.contract.BankContracts.UserIdentity;

@FunctionalInterface
public interface TokenIssuer {
    /**
     * Delegate to the team's JWT utility. Include subject=email and userId/role claims,
     * set expiration, and sign with the configured persistent key. No password is supplied.
     */
    String generateToken(UserIdentity user);
}
