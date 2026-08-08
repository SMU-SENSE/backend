package com.aac.ieojwo.device.dto;
import com.aac.ieojwo.device.domain.PairingStatus;
import java.time.Instant;
public record CurrentPairingResponse(Long pairingId,PairingStatus status,Instant expiresAt,long remainingSeconds){}
