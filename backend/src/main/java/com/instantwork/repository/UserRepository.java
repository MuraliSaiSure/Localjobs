package com.instantwork.repository;

import com.instantwork.model.AccountStatus;
import com.instantwork.model.Role;
import com.instantwork.model.User;
import com.instantwork.model.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
    List<User> findByRole(Role role);
    List<User> findByVerificationStatus(VerificationStatus verificationStatus);
    List<User> findByAccountStatus(AccountStatus accountStatus);
}
