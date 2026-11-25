package com.aac.ieojwo.sentence.service;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/** OpenAI 응답 파싱과 실패 시 빈 결과(→ 라벨 대체) 경로. 실제 OpenAI 는 부르지 않는다. */
class OpenAiClientTests {

    @Test
    void parsesChatCompletionAndFailsSoftly() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        OpenAiClient client = new OpenAiClient(builder, "test-key", "test-model");

        server.expect(requestTo("https://api.openai.com/v1/chat/completions"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer test-key"))
                .andExpect(jsonPath("$.model").value("test-model"))
                .andExpect(jsonPath("$.messages[1].content").value("[밥] [먹다] [싫어요]"))
                .andRespond(withSuccess("""
                        {"choices":[{"message":{"role":"assistant","content":"\\"밥 먹기 싫어요\\"\\n(설명)"}}]}
                        """, MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://api.openai.com/v1/chat/completions")).andRespond(withServerError());

        assertThat(client.complete("system", "[밥] [먹다] [싫어요]").map(SentenceService::cleanUp))
                .contains("밥 먹기 싫어요");
        assertThat(client.complete("system", "[밥]")).isEmpty();
        server.verify();

        assertThat(new OpenAiClient(RestClient.builder(), "", "m").complete("s", "u")).isEmpty();
        assertThat(SentenceService.systemPrompt(com.aac.ieojwo.sentence.domain.SentenceStyle.CASUAL)).contains("반말");
    }
}
