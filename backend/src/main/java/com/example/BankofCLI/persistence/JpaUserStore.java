package com.example.BankofCLI.persistence;

import com.example.BankofCLI.models.User;
import com.example.BankofCLI.repositories.UserRepository;
import com.example.service.port.BankData.UserData;
import com.example.service.port.UserStore;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.Optional;

// Connects the service layer's UserStore port to the JPA UserRepository.
// Runs inside the calling service's @Transactional method (no transactions of its own).
@Component
public class JpaUserStore implements UserStore {

    private final UserRepository users;

    public JpaUserStore(UserRepository users) {
        this.users = users;
    }

    @Override
    public Optional<UserData> findById(String userId) {
        return users.findById(userId).map(JpaUserStore::toData);
    }

    @Override
    public Optional<UserData> findByEmail(String normalizedEmail) {
        return users.findByEmail(normalizedEmail).map(JpaUserStore::toData);
    }

    @Override
    public UserData create(String firstName, String lastName, String normalizedEmail, String passwordHash) {
        try {
            // saveAndFlush so a duplicate email fails HERE (unique constraint), not later at commit
            User saved = users.saveAndFlush(new User(firstName, lastName, normalizedEmail, passwordHash));
            return toData(saved);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateEmailException(e);
        }
    }

    static UserData toData(User user) {
        return new UserData(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getRole()
        );
    }
}
