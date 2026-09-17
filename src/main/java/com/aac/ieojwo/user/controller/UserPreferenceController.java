package com.aac.ieojwo.user.controller;
import com.aac.ieojwo.common.api.ApiResponse;
import com.aac.ieojwo.user.domain.*;
import com.aac.ieojwo.user.dto.UserResponse;
import com.aac.ieojwo.user.service.UserPreferenceService;
import jakarta.validation.Valid;import jakarta.validation.constraints.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;import org.springframework.security.oauth2.core.oidc.user.OidcUser;import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestController @RequestMapping("/api/v1/me/aac-users/{userId}")
public class UserPreferenceController{
 public record SentenceLevelRequest(@Min(1) @Max(4) int level){} public record StatusRequest(@NotNull UserStatus status){}
 private final UserPreferenceService service;public UserPreferenceController(UserPreferenceService service){this.service=service;}
 @PatchMapping("/sentence-level") public ApiResponse<UserResponse> level(@AuthenticationPrincipal OidcUser p,@PathVariable Long userId,@Valid @RequestBody SentenceLevelRequest r){return ApiResponse.ok(service.sentenceLevel(p,userId,r.level()),"사용자 언어 수준 설정이 저장되었습니다.");}
 @PatchMapping("/status") public ApiResponse<UserResponse> status(@AuthenticationPrincipal OidcUser p,@PathVariable Long userId,@Valid @RequestBody StatusRequest r){return ApiResponse.ok(service.status(p,userId,r.status()));}
 @GetMapping("/voice-preview") public ApiResponse<Map<String,String>> preview(@RequestParam VoiceType voiceType){return ApiResponse.ok(Map.of("voiceType",voiceType.name(),"sampleText","안녕하세요! 말모아입니다.","delivery","CLIENT_TTS"));}
}
