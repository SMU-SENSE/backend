package com.aac.ieojwo.common.config;

import com.aac.ieojwo.guardian.domain.Guardian;
import com.aac.ieojwo.guardian.domain.GuardianRole;
import com.aac.ieojwo.guardian.domain.UserGuardian;
import com.aac.ieojwo.guardian.repository.GuardianRepository;
import com.aac.ieojwo.guardian.repository.UserGuardianRepository;
import com.aac.ieojwo.symbol.domain.Symbol;
import com.aac.ieojwo.symbol.domain.SymbolCategory;
import com.aac.ieojwo.symbol.repository.SymbolCategoryRepository;
import com.aac.ieojwo.symbol.repository.SymbolRepository;
import com.aac.ieojwo.user.domain.AacUser;
import com.aac.ieojwo.user.domain.GridSize;
import com.aac.ieojwo.user.domain.UserMode;
import com.aac.ieojwo.user.repository.AacUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Configuration
@Profile("!postgres")
public class DataInitializer {

    @Bean
    CommandLineRunner seedData(
            SymbolCategoryRepository categoryRepository,
            SymbolRepository symbolRepository,
            AacUserRepository userRepository,
            GuardianRepository guardianRepository,
            UserGuardianRepository userGuardianRepository
    ) {
        return args -> initialize(
                categoryRepository,
                symbolRepository,
                userRepository,
                guardianRepository,
                userGuardianRepository
        );
    }

    @Transactional
    public void initialize(
            SymbolCategoryRepository categoryRepository,
            SymbolRepository symbolRepository,
            AacUserRepository userRepository,
            GuardianRepository guardianRepository,
            UserGuardianRepository userGuardianRepository
    ) {
        // 카테고리 코드는 기존 규칙대로 대문자. model.json 의 category 키는 이 코드의 소문자다(food ↔ FOOD).
        Map<String, SymbolCategory> categories = new LinkedHashMap<>();
        int order = 10;
        for (String[] category : CATEGORIES) {
            categories.put(category[0], getOrCreateCategory(categoryRepository, category[0], category[1], order));
            order += 10;
        }

        if (symbolRepository.count() == 0) {
            seedCards(symbolRepository, categories);
        }

        if (userRepository.count() == 0) {
            AacUser user = userRepository.save(AacUser.create("김민우", UserMode.SIMPLE, GridSize.GRID_2X2));
            Guardian guardian = guardianRepository.save(Guardian.create("최성희", "guardian@example.com", "010-0000-0000"));
            userGuardianRepository.save(UserGuardian.create(user, guardian, GuardianRole.PRIMARY, true));
        }
    }

    /** 추천 모델 명세(docs/AI_RECOMMENDATION_SPEC.md 2장) 순서. 문법(GRAMMAR)은 카드가 아직 없어 뺐다. */
    private static final String[][] CATEGORIES = {
            {"EMERGENCY", "긴급어"}, {"PEOPLE", "사람"}, {"FOOD", "음식"}, {"PLACE", "장소"}, {"BODY", "신체"},
            {"ACTION", "행동"}, {"EMOTION", "감정"}, {"DESCRIBE", "설명"}, {"CHAT", "대화"}
    };

    /**
     * seed/cards.csv (card_id,label,category,pattern,predict) → symbols. pattern 은 모델 빌드 전용이라 무시한다.
     * predict=0 인 카드는 전부 긴급어라 emergency 로 저장한다(추천 제외 = !emergency).
     */
    private void seedCards(SymbolRepository symbolRepository, Map<String, SymbolCategory> categories) {
        List<String> lines;
        try (InputStream in = new ClassPathResource("seed/cards.csv").getInputStream()) {
            lines = new String(in.readAllBytes(), StandardCharsets.UTF_8).lines().skip(1).toList();
        } catch (IOException e) {
            throw new UncheckedIOException("seed/cards.csv 를 읽을 수 없습니다.", e);
        }
        Map<String, Integer> orderInCategory = new HashMap<>();
        for (String line : lines) {
            if (line.isBlank()) {
                continue;
            }
            String[] cols = line.split(",", -1);
            String categoryCode = cols[2].trim().toUpperCase();
            SymbolCategory category = categories.get(categoryCode);
            if (category == null) {
                throw new IllegalStateException("cards.csv 에 알 수 없는 카테고리가 있습니다: " + cols[2]);
            }
            int displayOrder = orderInCategory.merge(categoryCode, 10, Integer::sum);
            boolean emergency = "0".equals(cols[4].trim());
            symbolRepository.save(Symbol.createCard(cols[0].trim(), category, cols[1].trim(), null,
                    emergency, displayOrder));
        }
    }

    private SymbolCategory getOrCreateCategory(
            SymbolCategoryRepository repository,
            String code,
            String name,
            int displayOrder
    ) {
        return repository.findAll().stream()
                .filter(category -> category.getCode().equals(code))
                .findFirst()
                .orElseGet(() -> repository.save(SymbolCategory.create(code, name, displayOrder)));
    }
}
