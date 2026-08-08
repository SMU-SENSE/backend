package com.aac.ieojwo.device.dto;
import com.aac.ieojwo.device.domain.DeviceType;
import jakarta.validation.constraints.*;
public record CodeClaimRequest(@NotBlank @Pattern(regexp="\\d{6}") String code,@NotBlank @Size(max=100) String deviceId,@Size(max=100) String deviceName,@NotNull DeviceType deviceType){}
