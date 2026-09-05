package com.aac.ieojwo.notification.service;

import com.aac.ieojwo.common.exception.ForbiddenException;
import com.aac.ieojwo.common.exception.ResourceNotFoundException;
import com.aac.ieojwo.guardian.domain.Guardian;
import com.aac.ieojwo.guardian.domain.UserGuardian;
import com.aac.ieojwo.guardian.repository.GuardianRepository;
import com.aac.ieojwo.guardian.repository.UserGuardianRepository;
import com.aac.ieojwo.guardian.service.GuardianAccessService;
import com.aac.ieojwo.notification.domain.GuardianNotification;
import com.aac.ieojwo.notification.dto.NotificationResponse;
import com.aac.ieojwo.notification.repository.GuardianNotificationRepository;
import com.aac.ieojwo.symbol.domain.Symbol;
import com.aac.ieojwo.user.domain.AacUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class NotificationService {

    private final UserGuardianRepository userGuardianRepository;
    private final GuardianNotificationRepository notificationRepository;
    private final GuardianRepository guardianRepository;
    private final GuardianAccessService guardianAccessService;
    private final FcmPushSender pushSender;

    public NotificationService(UserGuardianRepository userGuardianRepository,
                               GuardianNotificationRepository notificationRepository,
                               GuardianRepository guardianRepository,
                               GuardianAccessService guardianAccessService,
                               FcmPushSender pushSender) {
        this.userGuardianRepository = userGuardianRepository;
        this.notificationRepository = notificationRepository;
        this.guardianRepository = guardianRepository;
        this.guardianAccessService = guardianAccessService;
        this.pushSender = pushSender;
    }

    /** Fan out an in-app notification (+ best-effort push) to every guardian linked to this AAC user. */
    @Transactional
    public void notifyEmergencySymbolUsed(AacUser aacUser, Symbol symbol) {
        List<UserGuardian> links = userGuardianRepository.findAllByUserIdOrderByPrimaryGuardianDescIdAsc(aacUser.getId());
        for (UserGuardian link : links) {
            Guardian guardian = link.getGuardian();
            GuardianNotification notification = GuardianNotification.emergencySymbolUsed(guardian, aacUser, symbol);
            notificationRepository.save(notification);
            pushSender.send(guardian.getPushToken(), "긴급 알림", notification.getMessage());
        }
    }

    public List<NotificationResponse> findMyNotifications(OidcUser principal) {
        Guardian guardian = guardianAccessService.requireCurrentGuardian(principal);
        return notificationRepository.findAllByGuardianIdOrderByCreatedAtDesc(guardian.getId())
                .stream().map(NotificationResponse::from).toList();
    }

    @Transactional
    public void markRead(OidcUser principal, Long notificationId) {
        Guardian guardian = guardianAccessService.requireCurrentGuardian(principal);
        GuardianNotification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("알림을 찾을 수 없습니다."));
        if (!notification.getGuardian().getId().equals(guardian.getId())) {
            throw new ForbiddenException("본인의 알림만 확인할 수 있습니다.");
        }
        notification.markRead();
    }

    @Transactional
    public void registerPushToken(OidcUser principal, String token) {
        Guardian guardian = guardianAccessService.requireCurrentGuardian(principal);
        guardian.updatePushToken(token);
        guardianRepository.save(guardian);
    }
}
