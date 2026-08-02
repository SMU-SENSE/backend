package com.aac.ieojwo.guardian.repository;

import com.aac.ieojwo.guardian.domain.UserGuardian;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserGuardianRepository extends JpaRepository<UserGuardian, Long> {
    boolean existsByUserIdAndGuardianId(Long userId, Long guardianId);
    List<UserGuardian> findAllByUserIdOrderByPrimaryGuardianDescIdAsc(Long userId);
}
