package com.aac.ieojwo.guardian.repository;
import com.aac.ieojwo.guardian.domain.UserGuardian;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface UserGuardianRepository extends JpaRepository<UserGuardian, Long> {
 boolean existsByUserIdAndGuardianId(Long userId, Long guardianId);
 List<UserGuardian> findAllByUserIdOrderByPrimaryGuardianDescIdAsc(Long userId);
 List<UserGuardian> findAllByGuardianIdOrderByIdAsc(Long guardianId);
 Optional<UserGuardian> findByUserIdAndGuardianId(Long userId, Long guardianId);
}
