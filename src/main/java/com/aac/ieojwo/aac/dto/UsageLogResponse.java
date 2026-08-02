package com.aac.ieojwo.aac.dto;

import com.aac.ieojwo.aac.domain.SymbolUsageLog;
import com.aac.ieojwo.aac.domain.UsageAction;

import java.time.LocalDateTime;

public record UsageLogResponse(
        Long id,
        Long userId,
        Long symbolId,
        String symbolName,
        UsageAction action,
        LocalDateTime occurredAt
) {
    public static UsageLogResponse from(SymbolUsageLog log) {
        return new UsageLogResponse(
                log.getId(),
                log.getUser().getId(),
                log.getSymbol().getId(),
                log.getSymbol().getName(),
                log.getAction(),
                log.getOccurredAt()
        );
    }
}
