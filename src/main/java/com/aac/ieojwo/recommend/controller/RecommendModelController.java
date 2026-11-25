package com.aac.ieojwo.recommend.controller;

import com.aac.ieojwo.common.api.ApiResponse;
import com.aac.ieojwo.recommend.dto.RecommendModelResponse;
import com.aac.ieojwo.recommend.service.RecommendModelService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@Tag(name = "Recommend Model", description = "다음 카드 추천 모델(model.json) 배포")
@RestController
@RequestMapping("/api/v1/recommend-model")
public class RecommendModelController {

    private final RecommendModelService modelService;

    public RecommendModelController(RecommendModelService modelService) {
        this.modelService = modelService;
    }

    /** 앱은 version 이 캐시와 다를 때만 url 에서 새 파일을 받는다. */
    @GetMapping("/latest")
    public ApiResponse<RecommendModelResponse> latest() {
        String version = modelService.version();
        return ApiResponse.ok(new RecommendModelResponse(version, "/api/v1/recommend-model/files/" + version));
    }

    /** model.json 원본 그대로(ApiResponse 로 감싸지 않음). 버전별 URL 이라 내용이 바뀌지 않으므로 길게 캐시한다. */
    @GetMapping("/files/{version}")
    public ResponseEntity<byte[]> file(@PathVariable String version) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .cacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePrivate().immutable())
                .body(modelService.content(version));
    }
}
