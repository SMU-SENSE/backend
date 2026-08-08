package com.aac.ieojwo.device.dto;
import com.aac.ieojwo.device.domain.DeviceType;
import jakarta.validation.constraints.*;
public record QrClaimRequest(@NotBlank String token,@NotBlank @Size(max=100) String deviceId,@Size(max=100) String deviceName,@NotNull DeviceType deviceType){}
