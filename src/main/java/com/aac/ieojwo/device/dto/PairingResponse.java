package com.aac.ieojwo.device.dto;
import java.time.Instant;
public record PairingResponse(Long pairingId,String qrPayload,String inviteCode,Instant expiresAt,long remainingSeconds){}
