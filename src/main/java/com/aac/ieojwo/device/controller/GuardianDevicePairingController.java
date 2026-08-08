package com.aac.ieojwo.device.controller;
import com.aac.ieojwo.common.api.ApiResponse;
import com.aac.ieojwo.device.dto.*;
import com.aac.ieojwo.device.service.DevicePairingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/v1/me/aac-users/{userId}") @Tag(name="Device Pairing")
public class GuardianDevicePairingController {
 private final DevicePairingService service; public GuardianDevicePairingController(DevicePairingService service){this.service=service;}
 @PostMapping("/device-pairings") @ResponseStatus(HttpStatus.CREATED) public ApiResponse<PairingResponse> issue(@AuthenticationPrincipal OidcUser p,@PathVariable Long userId){return ApiResponse.ok(service.issue(p,userId),"기기 연결 세션이 발급되었습니다.");}
 @PostMapping("/device-pairings/refresh") public ApiResponse<PairingResponse> refresh(@AuthenticationPrincipal OidcUser p,@PathVariable Long userId){return ApiResponse.ok(service.issue(p,userId),"기기 연결 세션이 재발급되었습니다.");}
 @GetMapping("/device-pairings/current") public ApiResponse<CurrentPairingResponse> current(@AuthenticationPrincipal OidcUser p,@PathVariable Long userId){return ApiResponse.ok(service.current(p,userId));}
 @GetMapping("/devices") @Tag(name="Devices") public ApiResponse<List<DeviceResponse>> devices(@AuthenticationPrincipal OidcUser p,@PathVariable Long userId){return ApiResponse.ok(service.devices(p,userId));}
}
