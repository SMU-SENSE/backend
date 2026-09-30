package com.aac.ieojwo.user.dto;

import com.aac.ieojwo.user.domain.AacUser;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "AAC 사용자의 개인화 문장 추천용 의사소통 프로필")
public record CommunicationProfileResponse(
        Long aacUserId,
        int sentenceLevel,
        int maxRecommendedSentenceWords,
        boolean easyWordsPreferred,
        boolean abstractExpressionsRestricted,
        boolean complexGrammarRestricted,
        boolean conciseDirectPreferred
) {
    public static CommunicationProfileResponse from(AacUser user) {
        return new CommunicationProfileResponse(
                user.getId(), user.getSentenceLevel(), user.getMaxRecommendedSentenceWords(),
                user.isEasyWordsPreferred(), user.isAbstractExpressionsRestricted(),
                user.isComplexGrammarRestricted(), user.isConciseDirectPreferred());
    }
}
