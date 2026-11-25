package com.aac.ieojwo.aac.service;

import com.aac.ieojwo.aac.domain.SymbolUsageLog;
import com.aac.ieojwo.aac.domain.UsageAction;
import com.aac.ieojwo.aac.domain.UserFavoriteSymbol;
import com.aac.ieojwo.aac.domain.UtteranceLog;
import com.aac.ieojwo.aac.dto.CreateUsageLogRequest;
import com.aac.ieojwo.aac.dto.CreateUtteranceLogRequest;
import com.aac.ieojwo.aac.dto.UsageLogResponse;
import com.aac.ieojwo.aac.dto.UtteranceLogResponse;
import com.aac.ieojwo.aac.repository.SymbolUsageLogRepository;
import com.aac.ieojwo.aac.repository.UserFavoriteSymbolRepository;
import com.aac.ieojwo.aac.repository.UtteranceLogRepository;
import com.aac.ieojwo.common.exception.ConflictException;
import com.aac.ieojwo.common.exception.ResourceNotFoundException;
import com.aac.ieojwo.notification.service.NotificationService;
import com.aac.ieojwo.symbol.domain.Symbol;
import com.aac.ieojwo.symbol.dto.SymbolResponse;
import com.aac.ieojwo.symbol.service.SymbolService;
import com.aac.ieojwo.user.domain.AacUser;
import com.aac.ieojwo.user.service.UserService;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class AacUsageService {

    private final UserFavoriteSymbolRepository favoriteRepository;
    private final SymbolUsageLogRepository usageLogRepository;
    private final UtteranceLogRepository utteranceLogRepository;
    private final UserService userService;
    private final SymbolService symbolService;
    private final NotificationService notificationService;

    public AacUsageService(UserFavoriteSymbolRepository favoriteRepository,
                           SymbolUsageLogRepository usageLogRepository,
                           UtteranceLogRepository utteranceLogRepository,
                           UserService userService, SymbolService symbolService,
                           NotificationService notificationService) {
        this.favoriteRepository = favoriteRepository;
        this.usageLogRepository = usageLogRepository;
        this.utteranceLogRepository = utteranceLogRepository;
        this.userService = userService;
        this.symbolService = symbolService;
        this.notificationService = notificationService;
    }

    @Transactional
    public SymbolResponse addFavorite(OidcUser principal, Long userId, Long symbolId) {
        AacUser user = userService.requireAccessibleUser(principal, userId);
        Symbol symbol = symbolService.getUsableSymbol(symbolId, user);
        if (favoriteRepository.existsByUserIdAndSymbolId(userId, symbolId)) {
            throw new ConflictException("이미 즐겨찾기에 등록된 상징입니다.");
        }
        favoriteRepository.save(UserFavoriteSymbol.create(user, symbol));
        return SymbolResponse.from(symbol);
    }

    public List<SymbolResponse> findFavorites(OidcUser principal, Long userId) {
        userService.requireAccessibleUser(principal, userId);
        return favoriteRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(favorite -> SymbolResponse.from(favorite.getSymbol()))
                .toList();
    }

    @Transactional
    public void removeFavorite(OidcUser principal, Long userId, Long symbolId) {
        userService.requireAccessibleUser(principal, userId);
        UserFavoriteSymbol favorite = favoriteRepository.findByUserIdAndSymbolId(userId, symbolId)
                .orElseThrow(() -> new ResourceNotFoundException("즐겨찾기를 찾을 수 없습니다."));
        favoriteRepository.delete(favorite);
    }

    @Transactional
    public UsageLogResponse createUsageLog(OidcUser principal, Long userId, CreateUsageLogRequest request) {
        AacUser user = userService.requireAccessibleUser(principal, userId);
        Symbol symbol = symbolService.getUsableSymbol(request.symbolId(), user);
        LocalDateTime occurredAt = request.occurredAt() == null ? LocalDateTime.now() : request.occurredAt();
        SymbolUsageLog log = SymbolUsageLog.create(user, symbol, request.action(), occurredAt);
        UsageLogResponse response = UsageLogResponse.from(usageLogRepository.save(log));
        if (request.action() == UsageAction.SPEAK && symbol.isEmergency()) {
            notificationService.notifyEmergencySymbolUsed(user, symbol);
        }
        return response;
    }

    public List<SymbolResponse> findRecentSymbols(OidcUser principal, Long userId, int limit) {
        userService.requireAccessibleUser(principal, userId);
        int safeLimit = Math.max(1, Math.min(limit, 50));
        List<SymbolUsageLog> logs = usageLogRepository.findByUserIdAndActionOrderByOccurredAtDesc(
                userId, UsageAction.SELECT, PageRequest.of(0, 200));

        Map<Long, Symbol> uniqueSymbols = new LinkedHashMap<>();
        for (SymbolUsageLog log : logs) {
            uniqueSymbols.putIfAbsent(log.getSymbol().getId(), log.getSymbol());
            if (uniqueSymbols.size() >= safeLimit) {
                break;
            }
        }
        return uniqueSymbols.values().stream().map(SymbolResponse::from).toList();
    }

    @Transactional
    public UtteranceLogResponse createUtteranceLog(OidcUser principal, Long userId,
                                                   CreateUtteranceLogRequest request) {
        AacUser user = userService.requireAccessibleUser(principal, userId);
        symbolService.getUsableCards(request.cardCodes(), user);
        LocalDateTime occurredAt = request.occurredAt() == null ? LocalDateTime.now() : request.occurredAt();
        UtteranceLog log = UtteranceLog.create(user, request.cardCodes(), request.converted(),
                request.spoken(), occurredAt);
        return UtteranceLogResponse.from(utteranceLogRepository.save(log));
    }

    // ponytail: 페이지 없이 전체 반환(기기 교체 시 개인 n-gram 복원용). 기록이 수만 건 넘으면 since 커서 페이징 추가.
    public List<UtteranceLogResponse> findUtteranceLogs(OidcUser principal, Long userId, LocalDateTime since) {
        userService.requireAccessibleUser(principal, userId);
        LocalDateTime from = since == null ? LocalDateTime.of(1970, 1, 1, 0, 0) : since;
        return utteranceLogRepository.findAllByUserIdAndOccurredAtAfterOrderByOccurredAtAsc(userId, from)
                .stream()
                .map(UtteranceLogResponse::from)
                .toList();
    }
}
