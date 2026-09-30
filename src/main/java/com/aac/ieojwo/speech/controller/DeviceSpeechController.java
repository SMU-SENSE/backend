package com.aac.ieojwo.speech.controller;

import com.aac.ieojwo.common.api.ApiResponse;
import com.aac.ieojwo.device.domain.AacDevice;
import com.aac.ieojwo.device.service.DeviceAuthService;
import com.aac.ieojwo.speech.dto.SttEventRequest;
import com.aac.ieojwo.speech.dto.SttEventResponse;
import com.aac.ieojwo.speech.service.SttEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/device")
@Tag(name = "Device Interaction Events")
public class DeviceSpeechController {
    private final DeviceAuthService auth;
    private final SttEventService service;

    public DeviceSpeechController(DeviceAuthService auth, SttEventService service) {
        this.auth = auth;
        this.service = service;
    }

    @PostMapping("/stt-events")
    @Operation(summary = "STT 텍스트 결과 기록", description = "원본 음성 파일은 저장하지 않습니다.")
    public ApiResponse<SttEventResponse> record(
            @RequestHeader(value = "Authorization", required = false) String token,
            @Valid @RequestBody SttEventRequest request) {
        AacDevice device = auth.authenticate(token);
        return ApiResponse.ok(service.record(device, request));
    }
}
