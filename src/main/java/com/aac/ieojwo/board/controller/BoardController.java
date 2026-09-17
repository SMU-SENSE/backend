package com.aac.ieojwo.board.controller;

import com.aac.ieojwo.board.dto.BoardDtos.*;
import com.aac.ieojwo.board.service.BoardService;
import com.aac.ieojwo.common.api.ApiResponse;
import com.aac.ieojwo.live.LiveEventService;
import com.aac.ieojwo.user.service.AacUserAccessService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController @RequestMapping("/api/v1/me/aac-users/{userId}")
public class BoardController {
 private final BoardService service; private final LiveEventService live; private final AacUserAccessService access;
 public BoardController(BoardService service,LiveEventService live,AacUserAccessService access){this.service=service;this.live=live;this.access=access;}
 @GetMapping("/board") public ApiResponse<BoardResponse> board(@AuthenticationPrincipal OidcUser p,@PathVariable Long userId){return ApiResponse.ok(service.get(p,userId));}
 @GetMapping(value="/events",produces=MediaType.TEXT_EVENT_STREAM_VALUE) public SseEmitter events(@AuthenticationPrincipal OidcUser p,@PathVariable Long userId){access.requireAccessibleUser(p,userId);return live.subscribe(userId);}
 @PostMapping("/board/categories") @ResponseStatus(HttpStatus.CREATED) public ApiResponse<CategoryResponse> category(@AuthenticationPrincipal OidcUser p,@PathVariable Long userId,@Valid @RequestBody CreateCategoryRequest r){return ApiResponse.ok(service.createCategory(p,userId,r));}
 @PatchMapping("/board/categories/{categoryId}") public ApiResponse<CategoryResponse> categoryUpdate(@AuthenticationPrincipal OidcUser p,@PathVariable Long userId,@PathVariable Long categoryId,@Valid @RequestBody UpdateCategoryRequest r){return ApiResponse.ok(service.updateCategory(p,userId,categoryId,r));}
 @DeleteMapping("/board/categories/{categoryId}") @ResponseStatus(HttpStatus.NO_CONTENT) public void categoryDelete(@AuthenticationPrincipal OidcUser p,@PathVariable Long userId,@PathVariable Long categoryId){service.deleteCategory(p,userId,categoryId);}
 @PostMapping("/board/cards") @ResponseStatus(HttpStatus.CREATED) public ApiResponse<CardResponse> card(@AuthenticationPrincipal OidcUser p,@PathVariable Long userId,@Valid @RequestBody CreateCardRequest r){return ApiResponse.ok(service.createCard(p,userId,r));}
 @PatchMapping("/board/cards/{cardId}") public ApiResponse<CardResponse> cardUpdate(@AuthenticationPrincipal OidcUser p,@PathVariable Long userId,@PathVariable Long cardId,@Valid @RequestBody UpdateCardRequest r){return ApiResponse.ok(service.updateCard(p,userId,cardId,r));}
 @PatchMapping("/board/cards/{cardId}/favorite") public ApiResponse<CardResponse> favorite(@AuthenticationPrincipal OidcUser p,@PathVariable Long userId,@PathVariable Long cardId,@RequestBody FavoriteRequest r){return ApiResponse.ok(service.favorite(p,userId,cardId,r));}
 @DeleteMapping("/board/cards/{cardId}") @ResponseStatus(HttpStatus.NO_CONTENT) public void cardDelete(@AuthenticationPrincipal OidcUser p,@PathVariable Long userId,@PathVariable Long cardId){service.deleteCard(p,userId,cardId);}
}
