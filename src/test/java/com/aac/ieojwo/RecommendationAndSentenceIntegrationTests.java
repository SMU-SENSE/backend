package com.aac.ieojwo;

import com.aac.ieojwo.account.domain.AccountType;
import com.aac.ieojwo.auth.dto.OnboardingRequest;
import com.aac.ieojwo.auth.service.AuthService;
import com.aac.ieojwo.symbol.repository.SymbolCategoryRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** 추천 모델 배포, 101장 카드 시드, 커스텀 카드, 문장 변환(LLM 대체 경로), 문장 단위 사용 기록. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RecommendationAndSentenceIntegrationTests {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired AuthService authService;
    @Autowired SymbolCategoryRepository categories;

    @Test
    void modelIsServedByVersion() throws Exception {
        mvc.perform(get("/api/v1/recommend-model/latest")).andExpect(status().isUnauthorized());
        onboard("model");

        String url = json.readTree(mvc.perform(get("/api/v1/recommend-model/latest").with(as("model")))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.data.version").value("2026.11.12-3"))
                        .andReturn().getResponse().getContentAsString())
                .path("data").path("url").asText();

        mvc.perform(get(url).with(as("model")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value("2026.11.12-3"))
                .andExpect(jsonPath("$.card['<s>'][0][0]").value("ic_people_me"));
        mvc.perform(get("/api/v1/recommend-model/files/1999.01.01-0").with(as("model")))
                .andExpect(status().isNotFound());
    }

    @Test
    void seededCardsCarryCardCodesAndEmergencyIsNotPredicted() throws Exception {
        onboard("seed");
        mvc.perform(get("/api/v1/symbols").with(as("seed")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(101));
        mvc.perform(get("/api/v1/symbols").param("emergency", "true").with(as("seed")))
                .andExpect(jsonPath("$.data.length()").value(6))
                .andExpect(jsonPath("$.data[0].cardCode").value("ic_emergency_help"))
                .andExpect(jsonPath("$.data[0].predict").value(false));
    }

    @Test
    void customCardsAreVisibleOnlyToTheirOwnerAndSoftDeleted() throws Exception {
        onboard("owner");
        onboard("stranger");
        long ownerUser = createUser("owner");
        long strangerUser = createUser("stranger");
        long food = categories.findAll().stream().filter(c -> c.getCode().equals("FOOD")).findFirst().orElseThrow().getId();

        JsonNode created = json.readTree(post(as("owner"), "/api/v1/me/aac-users/{id}/custom-symbols", ownerUser,
                Map.of("categoryId", food, "name", "딸기우유"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.cardCode").value(startsWith("ic_custom_")))
                .andExpect(jsonPath("$.data.custom").value(true))
                .andReturn().getResponse().getContentAsString()).path("data");
        String code = created.path("cardCode").asText();
        long symbolId = created.path("id").asLong();

        mvc.perform(get("/api/v1/symbols").with(as("owner"))).andExpect(jsonPath("$.data.length()").value(101));
        mvc.perform(get("/api/v1/symbols/{id}", symbolId).with(as("owner"))).andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/me/aac-users/{id}/custom-symbols", ownerUser).with(as("stranger")))
                .andExpect(status().isForbidden());
        post(as("stranger"), "/api/v1/me/aac-users/{id}/sentences", strangerUser, Map.of("cardCodes", List.of(code)))
                .andExpect(status().isNotFound());
        post(as("stranger"), "/api/v1/me/aac-users/{id}/favorites", strangerUser, Map.of("symbolId", symbolId))
                .andExpect(status().isNotFound());
        post(as("owner"), "/api/v1/me/aac-users/{id}/sentences", ownerUser, Map.of("cardCodes", List.of(code)))
                .andExpect(jsonPath("$.data.sentence").value("딸기우유"));

        mvc.perform(delete("/api/v1/me/aac-users/{id}/custom-symbols/{sid}", ownerUser, symbolId)
                        .with(as("owner")).with(csrf()))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/v1/me/aac-users/{id}/custom-symbols", ownerUser).with(as("owner")))
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    void sentenceFallsBackToJoinedLabelsWithoutLlm() throws Exception {
        onboard("talk");
        long user = createUser("talk");
        post(as("talk"), "/api/v1/me/aac-users/{id}/sentences", user,
                Map.of("cardCodes", List.of("ic_food_rice", "ic_action_eat", "ic_emotion_dislike"), "style", "POLITE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sentence").value("밥 먹다 싫어요"))
                .andExpect(jsonPath("$.data.generated").value(false));
        post(as("talk"), "/api/v1/me/aac-users/{id}/sentences", user, Map.of("cardCodes", List.of("ic_nope_nope")))
                .andExpect(status().isNotFound());
        post(as("talk"), "/api/v1/me/aac-users/{id}/sentences", user, Map.of("cardCodes", List.of()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void utteranceLogsKeepCardOrderAndRequireOwnership() throws Exception {
        onboard("log");
        onboard("log-other");
        long user = createUser("log");
        List<String> cards = List.of("ic_people_mom", "ic_food_water", "ic_people_mom");

        post(as("log"), "/api/v1/me/aac-users/{id}/utterance-logs", user,
                Map.of("cardCodes", cards, "converted", true, "spoken", true))
                .andExpect(status().isCreated());
        post(as("log"), "/api/v1/me/aac-users/{id}/utterance-logs", user,
                Map.of("cardCodes", List.of("ic_food_water"), "converted", false, "spoken", true))
                .andExpect(status().isCreated());
        post(as("log"), "/api/v1/me/aac-users/{id}/utterance-logs", user,
                Map.of("cardCodes", List.of("ic_nope_nope"), "converted", false, "spoken", true))
                .andExpect(status().isNotFound());
        post(as("log-other"), "/api/v1/me/aac-users/{id}/utterance-logs", user,
                Map.of("cardCodes", cards, "converted", false, "spoken", false))
                .andExpect(status().isForbidden());

        mvc.perform(get("/api/v1/me/aac-users/{id}/utterance-logs", user).with(as("log")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].cardCodes[0]").value("ic_people_mom"))
                .andExpect(jsonPath("$.data[0].cardCodes[2]").value("ic_people_mom"))
                .andExpect(jsonPath("$.data[0].converted").value(true))
                .andExpect(jsonPath("$.data[1].converted").value(false));
    }

    private ResultActions post(RequestPostProcessor who, String path, long id, Object body) throws Exception {
        return mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(path, id)
                .with(who).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(body)));
    }

    private RequestPostProcessor as(String subject) {
        return oidcLogin().idToken(token -> token.subject(subject));
    }

    private void onboard(String subject) {
        String email = subject + "@example.com";
        authService.upsertGoogleAccount(subject, email, "테스트 보호자", null);
        OidcIdToken token = new OidcIdToken("token", Instant.now(), Instant.now().plusSeconds(60),
                Map.of("sub", subject, "email", email));
        authService.completeOnboarding(new DefaultOidcUser(List.of(), token),
                new OnboardingRequest(AccountType.GUARDIAN, true, true, false, "010-1234-5678"));
    }

    private long createUser(String subject) throws Exception {
        String body = post(as(subject), "/api/v1/me/aac-users", 0, Map.of(
                "name", subject + " 사용자",
                "birthDate", "2012-01-15",
                "relationshipType", "PARENT",
                "emergencyContact", "01012345678"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(body).path("data").path("id").asLong();
    }
}
