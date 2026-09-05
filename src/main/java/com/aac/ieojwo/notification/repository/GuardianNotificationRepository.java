package com.aac.ieojwo.notification.repository;

import com.aac.ieojwo.notification.domain.GuardianNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GuardianNotificationRepository extends JpaRepository<GuardianNotification, Long> {
    List<GuardianNotification> findAllByGuardianIdOrderByCreatedAtDesc(Long guardianId);
}
