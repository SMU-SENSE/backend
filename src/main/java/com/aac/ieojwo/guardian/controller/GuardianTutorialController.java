package com.aac.ieojwo.guardian.controller;
import com.aac.ieojwo.common.api.ApiResponse;
import com.aac.ieojwo.guardian.service.GuardianTutorialService;
import com.aac.ieojwo.guardian.service.GuardianTutorialService.TutorialResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/me/tutorial")
public class GuardianTutorialController{
 private final GuardianTutorialService service;public GuardianTutorialController(GuardianTutorialService service){this.service=service;}
 @GetMapping public ApiResponse<TutorialResponse> get(@AuthenticationPrincipal OidcUser p){return ApiResponse.ok(service.current(p));}
 @PostMapping("/complete") public ApiResponse<TutorialResponse> complete(@AuthenticationPrincipal OidcUser p){return ApiResponse.ok(service.complete(p),"보호자 튜토리얼을 완료했습니다.");}
}
