package com.aac.ieojwo.device.controller;

import com.aac.ieojwo.common.api.ApiResponse;
import com.aac.ieojwo.device.domain.AacDevice;
import com.aac.ieojwo.device.service.DeviceAuthService;
import com.aac.ieojwo.location.service.LocationService;
import com.aac.ieojwo.location.service.LocationService.LocationRequest;
import com.aac.ieojwo.location.service.LocationService.LocationResponse;
import com.aac.ieojwo.report.service.SensorService;
import com.aac.ieojwo.report.service.SensorService.ExpressionRequest;
import com.aac.ieojwo.report.service.SensorService.SensorRequest;
import com.aac.ieojwo.report.service.SensorService.SensorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/device")
@Tag(name = "Device Telemetry")
public class DeviceTelemetryController {
    private final DeviceAuthService auth;
    private final LocationService locations;
    private final SensorService sensors;

    public DeviceTelemetryController(DeviceAuthService auth, LocationService locations,
                                     SensorService sensors) {
        this.auth = auth;
        this.locations = locations;
        this.sensors = sensors;
    }

    @PostMapping("/locations")
    public ApiResponse<LocationResponse> location(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody LocationRequest request) {
        AacDevice device = auth.authenticate(token);
        return ApiResponse.ok(locations.record(device, request));
    }

    @PostMapping("/sensor-events")
    public ApiResponse<SensorResponse> sensor(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody SensorRequest request) {
        return ApiResponse.ok(sensors.record(auth.authenticate(token), request));
    }

    @PostMapping("/expression-events")
    @Operation(summary = "얼굴 표정 분석 결과 기록",
            description = "표정 enum과 confidence만 저장하며 원본 얼굴 이미지·영상은 받지 않습니다.")
    public ApiResponse<SensorResponse> expression(
            @RequestHeader(value = "Authorization", required = false) String token,
            @Valid @RequestBody ExpressionRequest request) {
        return ApiResponse.ok(sensors.recordExpression(auth.authenticate(token), request));
    }

    @PostMapping("/emergency")
    public ApiResponse<Map<String, String>> emergency(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody(required = false) Map<String, String> body) {
        sensors.emergency(auth.authenticate(token), body == null ? null : body.get("message"));
        return ApiResponse.ok(Map.of("status", "EMERGENCY"));
    }
}
