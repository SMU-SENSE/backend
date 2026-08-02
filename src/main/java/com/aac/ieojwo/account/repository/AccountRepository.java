package com.aac.ieojwo.account.repository;

import com.aac.ieojwo.account.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByEmailIgnoreCase(String email);
}
