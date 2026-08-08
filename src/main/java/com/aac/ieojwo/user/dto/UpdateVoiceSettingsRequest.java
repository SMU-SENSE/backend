package com.aac.ieojwo.user.dto;
import com.aac.ieojwo.user.domain.VoiceType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public record UpdateVoiceSettingsRequest(@NotNull VoiceType voiceType,
 @NotNull @DecimalMin("0.7") @DecimalMax("1.3") BigDecimal speechRate) {}
