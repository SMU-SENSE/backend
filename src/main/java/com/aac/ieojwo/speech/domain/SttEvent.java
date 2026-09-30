package com.aac.ieojwo.speech.domain;

import com.aac.ieojwo.common.domain.BaseTimeEntity;
import com.aac.ieojwo.device.domain.AacDevice;
import com.aac.ieojwo.user.domain.AacUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "stt_events", indexes = @Index(name = "idx_stt_user_time",
        columnList = "aac_user_id,recorded_at"))
public class SttEvent extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "aac_user_id", nullable = false)
    private AacUser user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "device_id", nullable = false)
    private AacDevice device;

    @Column(length = 2000)
    private String recognizedText;

    @Column(nullable = false)
    private boolean successful;

    private Double confidence;

    @Column(nullable = false)
    private Instant recordedAt;

    protected SttEvent() {
    }

    public static SttEvent create(AacDevice device, String recognizedText, boolean successful,
                                  Double confidence, Instant recordedAt) {
        SttEvent event = new SttEvent();
        event.user = device.getUser();
        event.device = device;
        event.recognizedText = recognizedText == null || recognizedText.isBlank()
                ? null : recognizedText.trim();
        event.successful = successful;
        event.confidence = confidence;
        event.recordedAt = recordedAt == null ? Instant.now() : recordedAt;
        return event;
    }

    public Long getId() { return id; }
    public AacUser getUser() { return user; }
    public AacDevice getDevice() { return device; }
    public String getRecognizedText() { return recognizedText; }
    public boolean isSuccessful() { return successful; }
    public Double getConfidence() { return confidence; }
    public Instant getRecordedAt() { return recordedAt; }
}
