package com.aac.ieojwo.recommend.service;

import com.aac.ieojwo.common.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

/**
 * 다음 카드 추천 모델(model.json)을 앱에 배포한다. 추천 계산은 앱(기기)에서 하므로 서버는 파일만 내려준다.
 * 시작할 때 한 번 읽어서 메모리에 두고, 파일이 없거나 version 이 없으면 기동을 실패시킨다.
 */
// ponytail: 현재 버전 1개만 서빙. 버전별 보관/롤백이 필요하면 model-location 을 디렉터리나 스토리지로 확장.
@Service
public class RecommendModelService {

    private final byte[] content;
    private final String version;

    public RecommendModelService(@Value("${app.recommend.model-location:classpath:models/ngram/model.json}") Resource modelLocation,
                                 ObjectMapper objectMapper) {
        try (InputStream in = modelLocation.getInputStream()) {
            this.content = in.readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("추천 모델 파일을 읽을 수 없습니다: " + modelLocation, e);
        }
        try {
            this.version = objectMapper.readTree(content).path("version").asText("");
        } catch (IOException e) {
            throw new IllegalStateException("추천 모델 파일이 올바른 JSON이 아닙니다: " + modelLocation, e);
        }
        if (version.isBlank()) {
            throw new IllegalStateException("추천 모델 파일에 version 이 없습니다: " + modelLocation);
        }
    }

    public String version() {
        return version;
    }

    public byte[] content(String requestedVersion) {
        if (!version.equals(requestedVersion)) {
            throw new ResourceNotFoundException("추천 모델 버전을 찾을 수 없습니다. version=" + requestedVersion);
        }
        return content;
    }
}
