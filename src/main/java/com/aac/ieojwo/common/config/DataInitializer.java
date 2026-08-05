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
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
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
        Map<String, SymbolCategory> categories = new LinkedHashMap<>();
        categories.put("FOOD", getOrCreateCategory(categoryRepository, "FOOD", "음식", 10));
        categories.put("EMOTION", getOrCreateCategory(categoryRepository, "EMOTION", "감정", 20));
        categories.put("PEOPLE", getOrCreateCategory(categoryRepository, "PEOPLE", "사람", 30));
        categories.put("PLACE", getOrCreateCategory(categoryRepository, "PLACE", "장소", 40));
        categories.put("EMERGENCY", getOrCreateCategory(categoryRepository, "EMERGENCY", "긴급어", 50));
        categories.put("SOCIAL", getOrCreateCategory(categoryRepository, "SOCIAL", "인사·사회어", 60));
        categories.put("TIME", getOrCreateCategory(categoryRepository, "TIME", "시간", 70));
        categories.put("ENDING", getOrCreateCategory(categoryRepository, "ENDING", "어미", 80));
        categories.put("BODY", getOrCreateCategory(categoryRepository, "BODY", "신체", 90));

        if (symbolRepository.count() == 0) {
            symbolRepository.save(Symbol.create(categories.get("FOOD"), "물", null, "물 주세요", false, 10));
            symbolRepository.save(Symbol.create(categories.get("FOOD"), "밥", null, "밥 먹고 싶어요", false, 20));
            symbolRepository.save(Symbol.create(categories.get("EMOTION"), "좋아요", null, "좋아요", false, 10));
            symbolRepository.save(Symbol.create(categories.get("EMOTION"), "싫어요", null, "싫어요", false, 20));
            symbolRepository.save(Symbol.create(categories.get("EMOTION"), "불안해요", null, "불안해요", false, 30));
            symbolRepository.save(Symbol.create(categories.get("PEOPLE"), "엄마", null, "엄마", false, 10));
            symbolRepository.save(Symbol.create(categories.get("PEOPLE"), "선생님", null, "선생님", false, 20));
            symbolRepository.save(Symbol.create(categories.get("PLACE"), "화장실", null, "화장실에 가고 싶어요", false, 10));
            symbolRepository.save(Symbol.create(categories.get("PLACE"), "집", null, "집에 가고 싶어요", false, 20));
            symbolRepository.save(Symbol.create(categories.get("EMERGENCY"), "도와주세요", null, "도와주세요", true, 10));
            symbolRepository.save(Symbol.create(categories.get("EMERGENCY"), "아파요", null, "아파요", true, 20));
            symbolRepository.save(Symbol.create(categories.get("EMERGENCY"), "시끄러워요", null, "너무 시끄러워요", true, 30));
            symbolRepository.save(Symbol.create(categories.get("EMERGENCY"), "쉬고 싶어요", null, "잠깐 쉬고 싶어요", true, 40));
            symbolRepository.save(Symbol.create(categories.get("SOCIAL"), "네", null, "네", false, 10));
            symbolRepository.save(Symbol.create(categories.get("SOCIAL"), "아니요", null, "아니요", false, 20));
            symbolRepository.save(Symbol.create(categories.get("SOCIAL"), "잠깐만요", null, "잠깐만요", false, 30));
            symbolRepository.save(Symbol.create(categories.get("SOCIAL"), "몰라요", null, "잘 모르겠어요", false, 40));
            symbolRepository.save(Symbol.create(categories.get("BODY"), "머리", null, "머리가 아파요", false, 10));
            symbolRepository.save(Symbol.create(categories.get("BODY"), "배", null, "배가 아파요", false, 20));
        }

        if (userRepository.count() == 0) {
            AacUser user = userRepository.save(AacUser.create("김민우", UserMode.SIMPLE, GridSize.GRID_2X2));
            Guardian guardian = guardianRepository.save(Guardian.create("최성희", "guardian@example.com", "010-0000-0000"));
            userGuardianRepository.save(UserGuardian.create(user, guardian, GuardianRole.PRIMARY, true));
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
