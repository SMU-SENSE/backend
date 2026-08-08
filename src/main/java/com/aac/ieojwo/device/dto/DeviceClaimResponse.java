package com.aac.ieojwo.device.dto;
import java.time.Instant;
public record DeviceClaimResponse(Long aacUserId,String deviceId,Instant pairedAt){}
