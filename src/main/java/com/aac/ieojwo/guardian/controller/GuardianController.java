package com.aac.ieojwo.guardian.controller;

import com.aac.ieojwo.common.api.ApiResponse;
import com.aac.ieojwo.guardian.dto.CreateGuardianRequest;
import com.aac.ieojwo.guardian.dto.GuardianResponse;
import com.aac.ieojwo.guardian.dto.LinkGuardianRequest;
import com.aac.ieojwo.guardian.service.GuardianService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class GuardianController {

    private final GuardianService guardianService;

    public GuardianController(GuardianService guardianService) {
        this.guardianService = guardianService;
    }

    @PostMapping("/guardians")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<GuardianResponse> create(@Valid @RequestBody CreateGuardianRequest request) {
        return ApiResponse.ok(guardianService.create(request), "보호자가 생성되었습니다.");
    }

    @GetMapping("/guardians")
    public ApiResponse<List<GuardianResponse>> findAll() {
        return ApiResponse.ok(guardianService.findAll());
    }

    @PostMapping("/users/{userId}/guardians")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<GuardianResponse> linkToUser(
            @PathVariable Long userId,
            @Valid @RequestBody LinkGuardianRequest request
    ) {
        return ApiResponse.ok(guardianService.linkToUser(userId, request), "사용자와 보호자가 연결되었습니다.");
    }

    @GetMapping("/users/{userId}/guardians")
    public ApiResponse<List<GuardianResponse>> findByUser(@PathVariable Long userId) {
        return ApiResponse.ok(guardianService.findByUser(userId));
    }
}
