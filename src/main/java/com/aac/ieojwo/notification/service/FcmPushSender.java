package com.aac.ieojwo.notification.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * Best-effort FCM push. The {@link com.aac.ieojwo.notification.domain.GuardianNotification} row saved by
 * {@link NotificationService} is the source of truth — a push failure (or missing credentials) is logged and
 * swallowed, never thrown, so notification creation never fails because a phone push didn't go out.
 */
@Service
public class FcmPushSender {

    private static final Logger log = LoggerFactory.getLogger(FcmPushSender.class);

    private final String credentialsPath;
    private boolean enabled;

    public FcmPushSender(@Value("${app.fcm.credentials-path:}") String credentialsPath) {
        this.credentialsPath = credentialsPath;
    }

    @PostConstruct
    void init() {
        if (credentialsPath == null || credentialsPath.isBlank()) {
            log.info("FCM 자격증명이 설정되지 않아 푸시 발송을 건너뜁니다. (app.fcm.credentials-path)");
            return;
        }
        try (FileInputStream serviceAccount = new FileInputStream(credentialsPath)) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
            enabled = true;
        } catch (IOException e) {
            log.warn("FCM 초기화에 실패해 푸시 발송을 건너뜁니다: {}", e.getMessage());
        }
    }

    public void send(String token, String title, String body) {
        if (!enabled || token == null || token.isBlank()) {
            return;
        }
        Message message = Message.builder()
                .setToken(token)
                .setNotification(Notification.builder().setTitle(title).setBody(body).build())
                .build();
        try {
            FirebaseMessaging.getInstance().send(message);
        } catch (FirebaseMessagingException e) {
            log.warn("푸시 발송 실패: {}", e.getMessage());
        }
    }
}
