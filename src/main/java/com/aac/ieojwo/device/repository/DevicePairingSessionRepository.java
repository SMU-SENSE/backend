package com.aac.ieojwo.device.repository;
import com.aac.ieojwo.device.domain.*;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;
public interface DevicePairingSessionRepository extends JpaRepository<DevicePairingSession,Long>{
 @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select s from DevicePairingSession s where s.qrTokenHash=:hash") Optional<DevicePairingSession> findByQrHashForUpdate(@Param("hash")String hash);
 @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select s from DevicePairingSession s where s.inviteCodeHash=:hash") Optional<DevicePairingSession> findByCodeHashForUpdate(@Param("hash")String hash);
 Optional<DevicePairingSession> findFirstByUserIdAndStatusOrderByIdDesc(Long userId,PairingStatus status);
 @Lock(LockModeType.PESSIMISTIC_WRITE)
 List<DevicePairingSession> findAllByUserIdAndStatus(Long userId,PairingStatus status);
 boolean existsByInviteCodeHash(String hash);
}

