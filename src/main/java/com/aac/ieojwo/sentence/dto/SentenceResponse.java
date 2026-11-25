package com.aac.ieojwo.sentence.dto;

/**
 * @param sentence  완성 문장. LLM 실패 시 카드 라벨을 공백으로 이은 값
 * @param generated LLM 이 만든 문장이면 true, 라벨 이어붙이기로 대체했으면 false
 */
public record SentenceResponse(String sentence, boolean generated) {
}
