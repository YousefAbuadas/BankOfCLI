package com.example.service;

import com.example.service.port.CurrentUserProvider;
import com.example.service.port.UserStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static com.example.service.BankServiceException.Code.UNAUTHENTICATED;

@Service
@Transactional(readOnly = true)
public class CurrentUserService {
    private final CurrentUserProvider principal;
    private final UserStore users;

    public CurrentUserService(CurrentUserProvider principal, UserStore users) {
        this.principal = principal;
        this.users = users;
    }

    public String requireUserId() {
        String id = principal.userId().filter(value -> !value.isBlank()).orElseThrow(CurrentUserService::missing);
        // A signed token does not allow access to a customer deleted since it was issued.
        if (users.findById(id).isEmpty()) throw missing();
        return id;
    }

    private static BankServiceException missing() {
        return new BankServiceException(UNAUTHENTICATED, "Please sign in to continue.");
    }
}
