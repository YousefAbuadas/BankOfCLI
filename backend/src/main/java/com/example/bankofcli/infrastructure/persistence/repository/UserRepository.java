package com.example.bankofcli.infrastructure.persistence.repository;

import com.example.bankofcli.infrastructure.persistence.entity.User;
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
