package com.aac.ieojwo.aac.dto;

import com.aac.ieojwo.aac.domain.UtteranceLog;

import java.time.LocalDateTime;
import java.util.List;

public record UtteranceLogResponse(
        Long id,
        Long userId,
        List<String> cardCodes,
        boolean converted,
        boolean spoken,
        LocalDateTime occurredAt
) {
    public static UtteranceLogResponse from(UtteranceLog log) {
        return new UtteranceLogResponse(log.getId(), log.getUser().getId(), log.getCardCodes(),
                log.isConverted(), log.isSpoken(), log.getOccurredAt());
    }
}
