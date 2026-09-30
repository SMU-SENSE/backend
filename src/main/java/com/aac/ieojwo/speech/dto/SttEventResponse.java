package com.aac.ieojwo.speech.dto;

import com.aac.ieojwo.speech.domain.SttEvent;

import java.time.Instant;

public record SttEventResponse(Long id, Long aacUserId, String deviceId, String recognizedText,
                               boolean successful, Double confidence, Instant occurredAt) {
    public static SttEventResponse from(SttEvent event) {
        return new SttEventResponse(event.getId(), event.getUser().getId(),
                event.getDevice().getDeviceId(), event.getRecognizedText(), event.isSuccessful(),
                event.getConfidence(), event.getRecordedAt());
    }
}
