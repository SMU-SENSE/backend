package com.aac.ieojwo.device.dto;
import com.aac.ieojwo.device.domain.*;
import java.time.Instant;
public record DeviceResponse(Long id,String deviceId,String deviceName,DeviceType deviceType,DeviceStatus status,Instant pairedAt,Instant lastSeenAt){public static DeviceResponse from(AacDevice d){return new DeviceResponse(d.getId(),d.getDeviceId(),d.getDeviceName(),d.getDeviceType(),d.getStatus(),d.getPairedAt(),d.getLastSeenAt());}}
