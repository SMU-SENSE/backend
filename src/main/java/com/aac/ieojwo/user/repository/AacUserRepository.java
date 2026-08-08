package com.aac.ieojwo.user.repository;
import com.aac.ieojwo.user.domain.AacUser;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
public interface AacUserRepository extends JpaRepository<AacUser,Long>{
 @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select u from AacUser u where u.id=:id") Optional<AacUser> findByIdForUpdate(@Param("id")Long id);
}
