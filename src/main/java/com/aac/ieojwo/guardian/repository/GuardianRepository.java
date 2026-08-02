package com.aac.ieojwo.guardian.repository;

import com.aac.ieojwo.guardian.domain.Guardian;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GuardianRepository extends JpaRepository<Guardian, Long> {
    boolean existsByEmail(String email);
    Optional<Guardian> findByEmailIgnoreCase(String email);
    Optional<Guardian> findByAccountId(Long accountId);
}
