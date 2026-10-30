package com.example.BankofCLI.repositories;

import com.example.BankofCLI.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    // Used for login and for rejecting duplicate emails at registration.
    // (No findByEmailAndPassword: passwords are BCrypt hashes, so AuthService
    //  compares them with PasswordEncoder.matches, not in SQL.)
    Optional<User> findByEmail(String email);
}
