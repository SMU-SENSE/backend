package com.aac.ieojwo.user.dto;
import com.aac.ieojwo.guardian.domain.*;
import com.aac.ieojwo.user.domain.*;
import java.math.BigDecimal;
import java.time.LocalDate;
public record OnboardingSummaryResponse(Long userId, String name, String profileImageUrl, LocalDate birthDate,
 RelationshipType relationshipType, String relationshipDetail, String emergencyContact, String notes,
 GridSize gridSize, VoiceType voiceType, BigDecimal speechRate, AacUserSetupStep setupStep) {
 public static OnboardingSummaryResponse from(AacUser user, UserGuardian link) {
  return new OnboardingSummaryResponse(user.getId(), user.getName(), user.getProfileImageUrl(), user.getBirthDate(),
   link.getRelationshipType(), link.getRelationshipDetail(), user.getEmergencyContact(), user.getNotes(),
   user.getGridSize(), user.getVoiceType(), user.getSpeechRate(), user.getSetupStep());
 }
}
