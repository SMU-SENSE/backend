package com.aac.ieojwo.device.controller;

import com.aac.ieojwo.aac.domain.UsageAction;
import com.aac.ieojwo.board.dto.BoardDtos.BoardResponse;
import com.aac.ieojwo.board.service.BoardService;
import com.aac.ieojwo.common.api.ApiResponse;
import com.aac.ieojwo.device.domain.AacDevice;
import com.aac.ieojwo.device.service.DeviceAuthService;
import com.aac.ieojwo.live.LiveEventService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.time.Instant;
import java.util.Map;

@RestController @RequestMapping("/api/v1/device")
public class DeviceSessionController{
 public record CardUsageRequest(@NotNull Long cardId,@NotNull UsageAction action,Instant occurredAt){}
 private final DeviceAuthService auth;private final BoardService board;private final LiveEventService live;
 public DeviceSessionController(DeviceAuthService auth,BoardService board,LiveEventService live){this.auth=auth;this.board=board;this.live=live;}
 @PostMapping("/heartbeat") public ApiResponse<Map<String,Object>> heartbeat(@RequestHeader(value="Authorization",required=false)String token){AacDevice d=auth.authenticate(token);return ApiResponse.ok(Map.of("deviceId",d.getDeviceId(),"aacUserId",d.getUser().getId(),"lastSeenAt",d.getLastSeenAt()));}
 @GetMapping("/board") public ApiResponse<BoardResponse> board(@RequestHeader(value="Authorization",required=false)String token){AacDevice d=auth.authenticate(token);return ApiResponse.ok(board.getForDevice(d.getUser()));}
 @PostMapping("/card-usage") public ApiResponse<Map<String,Long>> usage(@RequestHeader(value="Authorization",required=false)String token,@Valid @RequestBody CardUsageRequest r){AacDevice d=auth.authenticate(token);return ApiResponse.ok(Map.of("id",board.recordUsage(d.getUser(),r.cardId(),r.action(),r.occurredAt())));}
 @GetMapping(value="/events",produces=MediaType.TEXT_EVENT_STREAM_VALUE) public SseEmitter events(@RequestHeader(value="Authorization",required=false)String token){AacDevice d=auth.authenticate(token);return live.subscribe(d.getUser().getId());}
}
