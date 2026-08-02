package com.aac.ieojwo.aac.dto;

import com.aac.ieojwo.aac.domain.UsageAction;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateUsageLogRequest(
        @NotNull(message = "상징 ID는 필수입니다.")
        Long symbolId,

        @NotNull(message = "사용 동작은 필수입니다.")
        UsageAction action,

        LocalDateTime occurredAt
) {
}
