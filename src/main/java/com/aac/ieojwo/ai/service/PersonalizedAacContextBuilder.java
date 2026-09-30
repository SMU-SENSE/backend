package com.aac.ieojwo.ai.service;

import com.aac.ieojwo.ai.dto.PersonalizedAacContext;
import com.aac.ieojwo.ai.dto.PersonalizedAacContext.CommunicationPreferences;
import com.aac.ieojwo.ai.dto.PersonalizedAacContext.VocabularyItem;
import com.aac.ieojwo.ai.dto.VocabularyPriority;
import com.aac.ieojwo.board.domain.AacCard;
import com.aac.ieojwo.board.domain.CardUsageLog;
import com.aac.ieojwo.board.repository.AacCardRepository;
import com.aac.ieojwo.board.repository.CardUsageLogRepository;
import com.aac.ieojwo.user.domain.AacUser;
import com.aac.ieojwo.user.service.AacUserAccessService;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class PersonalizedAacContextBuilder {
    private static final int EVENT_LOOKBACK_LIMIT = 500;
    private static final int PRIORITIZED_VOCABULARY_LIMIT = 50;
    private static final int FREQUENT_CARD_LIMIT = 10;
    private static final int RECENT_CARD_LIMIT = 10;

    private final AacUserAccessService access;
    private final AacCardRepository cards;
    private final CardUsageLogRepository usageLogs;

    public PersonalizedAacContextBuilder(AacUserAccessService access, AacCardRepository cards,
                                         CardUsageLogRepository usageLogs) {
        this.access = access;
        this.cards = cards;
        this.usageLogs = usageLogs;
    }

    public PersonalizedAacContext build(OidcUser principal, Long userId, String currentSituation) {
        return build(access.requireAccessibleUser(principal, userId), currentSituation);
    }

    public PersonalizedAacContext build(AacUser user, String currentSituation) {
        List<AacCard> activeCards = cards.findAllByUserIdAndActiveTrueOrderByDisplayOrderAscIdAsc(user.getId());
        List<CardUsageLog> recentLogs = usageLogs
                .findAllByUserIdAndCardIsNotNullOrderByOccurredAtDesc(
                        user.getId(), PageRequest.of(0, EVENT_LOOKBACK_LIMIT));

        Map<Long, Long> frequency = new HashMap<>();
        Map<Long, Instant> lastUsed = new HashMap<>();
        LinkedHashSet<Long> recentCardIds = new LinkedHashSet<>();
        List<String> recentExpressions = new ArrayList<>();
        Set<String> seenExpressions = new HashSet<>();
        for (CardUsageLog log : recentLogs) {
            AacCard card = log.getCard();
            if (card == null) continue;
            frequency.merge(card.getId(), 1L, Long::sum);
            lastUsed.putIfAbsent(card.getId(), log.getOccurredAt());
            if (recentCardIds.size() < RECENT_CARD_LIMIT) recentCardIds.add(card.getId());
            if (recentExpressions.size() < RECENT_CARD_LIMIT && seenExpressions.add(card.getTtsText())) {
                recentExpressions.add(card.getTtsText());
            }
        }

        Set<Long> frequentCardIds = frequency.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .limit(FREQUENT_CARD_LIMIT)
                .map(Map.Entry::getKey)
                .collect(java.util.stream.Collectors.toSet());

        List<VocabularyItem> vocabulary = activeCards.stream()
                .map(card -> new VocabularyItem(
                        card.getId(), card.getText(), card.getTtsText(),
                        priority(card, frequentCardIds, recentCardIds), card.isImportant(),
                        card.isFavorite(), frequency.getOrDefault(card.getId(), 0L),
                        lastUsed.get(card.getId())))
                .sorted(Comparator.comparing(VocabularyItem::priority)
                        .thenComparing(VocabularyItem::usageCount, Comparator.reverseOrder())
                        .thenComparing(VocabularyItem::lastUsedAt,
                                Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(VocabularyItem::cardId))
                .limit(PRIORITIZED_VOCABULARY_LIMIT)
                .toList();

        return new PersonalizedAacContext(
                user.getId(), age(user), user.getNotes(),
                new CommunicationPreferences(
                        user.getSentenceLevel(), user.getMaxRecommendedSentenceWords(),
                        user.isEasyWordsPreferred(), user.isAbstractExpressionsRestricted(),
                        user.isComplexGrammarRestricted(), user.isConciseDirectPreferred()),
                vocabulary, List.copyOf(recentExpressions), normalize(currentSituation));
    }

    private VocabularyPriority priority(AacCard card, Set<Long> frequentCardIds,
                                        Set<Long> recentCardIds) {
        if (card.isImportant()) return VocabularyPriority.GUARDIAN_IMPORTANT;
        if (card.isFavorite()) return VocabularyPriority.FAVORITE;
        if (frequentCardIds.contains(card.getId())) return VocabularyPriority.FREQUENT;
        if (recentCardIds.contains(card.getId())) return VocabularyPriority.RECENT;
        if (card.isSystemCard()) return VocabularyPriority.CORE;
        return VocabularyPriority.CUSTOM;
    }

    private Integer age(AacUser user) {
        LocalDate birthDate = user.getBirthDate();
        return birthDate == null ? null : Period.between(birthDate, LocalDate.now()).getYears();
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
