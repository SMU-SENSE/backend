package com.aac.ieojwo;

import com.aac.ieojwo.account.domain.AccountType;
import com.aac.ieojwo.ai.spi.AacSentenceRecommendationProvider;
import com.aac.ieojwo.auth.dto.OnboardingRequest;
import com.aac.ieojwo.auth.service.AuthService;
import com.aac.ieojwo.board.domain.CardUsageLog;
import com.aac.ieojwo.board.dto.BoardDtos.BoardChangeEvent;
import com.aac.ieojwo.board.repository.CardUsageLogRepository;
import com.aac.ieojwo.live.LiveEventService;
import com.aac.ieojwo.report.repository.SensorEventRepository;
import com.aac.ieojwo.speech.repository.SttEventRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(PersonalizedAacIntegrationTests.ProviderConfiguration.class)
class PersonalizedAacIntegrationTests {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired AuthService auth;
    @Autowired CardUsageLogRepository usageLogs;
    @Autowired SttEventRepository sttEvents;
    @Autowired SensorEventRepository sensorEvents;
    @MockitoSpyBean LiveEventService live;

    @Test
    void profileCardsContextRecommendationAndEventContracts() throws Exception {
        onboard("owner", "owner@example.com");
        long userId = createUser("owner", "owner@example.com", "민수");

        mvc.perform(patch("/api/v1/me/aac-users/{id}/communication-profile", userId)
                        .with(login("owner", "owner@example.com")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sentenceLevel":3,"maxRecommendedSentenceWords":6,
                                "easyWordsPreferred":true,"abstractExpressionsRestricted":true,
                                "complexGrammarRestricted":true,"conciseDirectPreferred":true}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.maxRecommendedSentenceWords").value(6));
        mvc.perform(get("/api/v1/me/aac-users/{id}/communication-profile", userId)
                        .with(login("owner", "owner@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sentenceLevel").value(3))
                .andExpect(jsonPath("$.data.maxRecommendedSentenceWords").value(6));

        JsonNode board = data(mvc.perform(get("/api/v1/me/aac-users/{id}/board", userId)
                        .with(login("owner", "owner@example.com")))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        long categoryId = board.path("categories").get(0).path("id").asLong();
        clearInvocations(live);
        JsonNode custom = data(mvc.perform(post("/api/v1/me/aac-users/{id}/board/cards", userId)
                        .with(login("owner", "owner@example.com")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of(
                                "categoryId", categoryId, "displayText", "물을 원해요",
                                "ttsText", "물 주세요", "important", true, "displayOrder", 90))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.displayText").value("물을 원해요"))
                .andExpect(jsonPath("$.data.ttsText").value("물 주세요"))
                .andReturn().getResponse().getContentAsString());
        long customCardId = custom.path("id").asLong();
        clearInvocations(live);
        mvc.perform(patch("/api/v1/me/aac-users/{id}/board/cards/{cardId}", userId, customCardId)
                        .with(login("owner", "owner@example.com")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ttsText\":\"시원한 물 주세요\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.ttsText").value("시원한 물 주세요"));
        verify(live).publish(eq(userId), eq("BOARD_UPDATED"),
                org.mockito.ArgumentMatchers.argThat(payload -> payload instanceof BoardChangeEvent event
                        && "CARD_UPDATED".equals(event.changeType()) && event.card() != null
                        && "시원한 물 주세요".equals(event.card().ttsText())));

        JsonNode fallback = data(mvc.perform(post("/api/v1/me/aac-users/{id}/board/cards", userId)
                        .with(login("owner", "owner@example.com")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of(
                                "categoryId", categoryId, "displayText", "쉬어요", "displayOrder", 91))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.ttsText").value("쉬어요"))
                .andReturn().getResponse().getContentAsString());
        assertThat(fallback.path("systemCard").asBoolean()).isFalse();

        String deviceToken = pair(userId, "owner", "owner@example.com");
        mvc.perform(post("/api/v1/device/card-usage")
                        .header("Authorization", "Bearer " + deviceToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of(
                                "cardId", customCardId, "action", "SPEAK"))))
                .andExpect(status().isOk());
        CardUsageLog usage = usageLogs.findAll().getLast();
        assertThat(usage.getSpokenText()).isEqualTo("시원한 물 주세요");
        assertThat(usage.getDisplayTextSnapshot()).isEqualTo("물을 원해요");
        assertThat(usage.getDevice()).isNotNull();
        mvc.perform(post("/api/v1/device/card-usage")
                        .header("Authorization", "Bearer " + deviceToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of(
                                "cardId", customCardId, "action", "SPEAK"))))
                .andExpect(status().isOk());
        long fallbackCardId = fallback.path("id").asLong();
        mvc.perform(patch("/api/v1/me/aac-users/{id}/board/cards/{cardId}/favorite",
                        userId, fallbackCardId)
                        .with(login("owner", "owner@example.com")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"favorite\":true}"))
                .andExpect(status().isOk());
        mvc.perform(post("/api/v1/device/card-usage")
                        .header("Authorization", "Bearer " + deviceToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of(
                                "cardId", fallbackCardId, "action", "SPEAK"))))
                .andExpect(status().isOk());

        mvc.perform(post("/api/v1/device/stt-events")
                        .header("Authorization", "Bearer " + deviceToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"recognizedText\":\"물 주세요\",\"successful\":true,\"confidence\":0.92}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.recognizedText").value("물 주세요"));
        assertThat(sttEvents.findAll()).singleElement()
                .satisfies(event -> assertThat(event.getRecognizedText()).isEqualTo("물 주세요"));

        mvc.perform(post("/api/v1/device/expression-events")
                        .header("Authorization", "Bearer " + deviceToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"emotion\":\"HAPPY\",\"confidence\":0.81}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.type").value("EXPRESSION"))
                .andExpect(jsonPath("$.data.label").value("HAPPY"));
        assertThat(sensorEvents.findAll()).singleElement()
                .satisfies(event -> assertThat(event.getLabel()).isEqualTo("HAPPY"));

        mvc.perform(get("/api/v1/me/aac-users/{id}/ai/context", userId)
                        .with(login("owner", "owner@example.com"))
                        .param("currentSituation", "점심 시간"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.communicationPreferences.maxRecommendedSentenceWords").value(6))
                .andExpect(jsonPath("$.data.prioritizedVocabulary[0].priority").value("GUARDIAN_IMPORTANT"))
                .andExpect(jsonPath("$.data.prioritizedVocabulary[0].usageCount").value(2))
                .andExpect(jsonPath("$.data.prioritizedVocabulary[1].priority").value("FAVORITE"))
                .andExpect(jsonPath("$.data.recentExpressions[0]").value("쉬어요"))
                .andExpect(jsonPath("$.data.recentExpressions[1]").value("시원한 물 주세요"))
                .andExpect(jsonPath("$.data.currentSituation").value("점심 시간"));

        String recommendationBody = mvc.perform(post("/api/v1/me/aac-users/{id}/ai/recommendations", userId)
                        .with(login("owner", "owner@example.com")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentSituation\":\"점심 시간\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sentence").value("물 주세요."))
                .andReturn().getResponse().getContentAsString();
        assertThat(json.readTree(recommendationBody).path("data").size()).isEqualTo(1);
    }

    @Test
    void guardianCannotReadAnotherGuardiansPersonalizedContext() throws Exception {
        onboard("owner2", "owner2@example.com");
        long userId = createUser("owner2", "owner2@example.com", "소유자 사용자");
        onboard("intruder", "intruder@example.com");

        mvc.perform(get("/api/v1/me/aac-users/{id}/communication-profile", userId)
                        .with(login("intruder", "intruder@example.com")))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/me/aac-users/{id}/ai/context", userId)
                        .with(login("intruder", "intruder@example.com")))
                .andExpect(status().isForbidden());
        mvc.perform(patch("/api/v1/me/aac-users/{id}/communication-profile", userId)
                        .with(login("intruder", "intruder@example.com")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"sentenceLevel\":4}"))
                .andExpect(status().isForbidden());
    }

    private long createUser(String subject, String email, String name) throws Exception {
        return data(mvc.perform(post("/api/v1/me/aac-users")
                        .with(login(subject, email)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of(
                                "name", name, "birthDate", "2012-01-15",
                                "relationshipType", "PARENT", "emergencyContact", "01012345678"))))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString())
                .path("id").asLong();
    }

    private String pair(long userId, String subject, String email) throws Exception {
        JsonNode pairing = data(mvc.perform(post("/api/v1/me/aac-users/{id}/device-pairings", userId)
                        .with(login(subject, email)).with(csrf()))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
        JsonNode claim = data(mvc.perform(post("/api/v1/device-pairings/claim/code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of(
                                "code", pairing.path("inviteCode").asText(),
                                "deviceId", "device-" + subject, "deviceType", "TABLET"))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        return claim.path("accessToken").asText();
    }

    private JsonNode data(String body) throws Exception {
        return json.readTree(body).path("data");
    }

    private void onboard(String subject, String email) {
        auth.upsertGoogleAccount(subject, email, "보호자", null);
        auth.completeOnboarding(principal(subject, email),
                new OnboardingRequest(AccountType.GUARDIAN, true, true, false, "01012345678"));
    }

    private OidcUser principal(String subject, String email) {
        Instant now = Instant.now();
        OidcIdToken token = new OidcIdToken("token", now, now.plusSeconds(300),
                Map.of("sub", subject, "email", email));
        return new DefaultOidcUser(List.of(), token,
                OidcUserInfo.builder().subject(subject).email(email).build());
    }

    private org.springframework.test.web.servlet.request.RequestPostProcessor login(String subject,
                                                                                    String email) {
        return oidcLogin().idToken(token -> token.subject(subject).claim("email", email));
    }

    @TestConfiguration
    static class ProviderConfiguration {
        @Bean
        AacSentenceRecommendationProvider testRecommendationProvider() {
            return prompt -> {
                assertThat(prompt.context()).isNotNull();
                assertThat(prompt.contextJson()).contains("prioritizedVocabulary", "currentSituation");
                return "물 주세요.";
            };
        }
    }
}
