package com.aac.ieojwo.user.controller;

import com.aac.ieojwo.common.api.ApiResponse;
import com.aac.ieojwo.user.dto.CreateUserRequest;
import com.aac.ieojwo.user.dto.UpdateUserSettingsRequest;
import com.aac.ieojwo.user.dto.UserResponse;
import com.aac.ieojwo.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
        return ApiResponse.ok(userService.create(request), "사용자가 생성되었습니다.");
    }

    @GetMapping
    public ApiResponse<List<UserResponse>> findAll() {
        return ApiResponse.ok(userService.findAll());
    }

    @GetMapping("/{userId}")
    public ApiResponse<UserResponse> findById(@PathVariable Long userId) {
        return ApiResponse.ok(userService.findById(userId));
    }

    @PatchMapping("/{userId}/settings")
    public ApiResponse<UserResponse> updateSettings(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserSettingsRequest request
    ) {
        return ApiResponse.ok(userService.updateSettings(userId, request), "사용자 설정이 변경되었습니다.");
    }
}
