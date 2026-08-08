package com.aac.ieojwo.device.controller;
import com.aac.ieojwo.common.api.ApiResponse;
import com.aac.ieojwo.device.dto.*;
import com.aac.ieojwo.device.service.DevicePairingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/device-pairings/claim") @Tag(name="Device Pairing")
public class DevicePairingClaimController {
 private final DevicePairingService service; public DevicePairingClaimController(DevicePairingService service){this.service=service;}
 @PostMapping("/qr") public ApiResponse<DeviceClaimResponse> qr(@Valid @RequestBody QrClaimRequest request){return ApiResponse.ok(service.claimQr(request),"기기가 연결되었습니다.");}
 @PostMapping("/code") public ApiResponse<DeviceClaimResponse> code(@Valid @RequestBody CodeClaimRequest request){return ApiResponse.ok(service.claimCode(request),"기기가 연결되었습니다.");}
}
