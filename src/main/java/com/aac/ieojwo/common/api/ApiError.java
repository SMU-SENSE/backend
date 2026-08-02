package com.aac.ieojwo.common.api;

import java.time.LocalDateTime;
import java.util.Map;

public record ApiError(
        boolean success,
        String code,
        String message,
        Map<String, String> fieldErrors,
        LocalDateTime timestamp
) {
    public static ApiError of(String code, String message) {
        return new ApiError(false, code, message, Map.of(), LocalDateTime.now());
    }

    public static ApiError of(String code, String message, Map<String, String> fieldErrors) {
        return new ApiError(false, code, message, fieldErrors, LocalDateTime.now());
    }
}
