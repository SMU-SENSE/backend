package com.aac.ieojwo.aac.domain;

import com.aac.ieojwo.user.domain.AacUser;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 카드 묶음 하나를 실제로 사용(말하기/완성)한 기록. LLM 변환 여부와 상관없이 카드 순서만 남긴다.
 * 개인 n-gram 복원용이라 변환된 문장 텍스트는 저장하지 않는다.
 */
@Entity
@Table(name = "utterance_logs", indexes = @Index(name = "idx_utterance_user_time", columnList = "user_id, occurred_at"))
public class UtteranceLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AacUser user;

    /** model.json 키와 같은 형식: 카드 ID를 공백으로 이은 문자열. 카드 ID는 바뀌지 않는 규칙이라 FK 없이 둔다. */
    @Column(name = "card_codes", nullable = false, length = 2000)
    private String cardCodes;

    @Column(nullable = false)
    private boolean converted;

    @Column(nullable = false)
    private boolean spoken;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    protected UtteranceLog() {
    }

    public static UtteranceLog create(AacUser user, List<String> cardCodes, boolean converted,
                                      boolean spoken, LocalDateTime occurredAt) {
        UtteranceLog log = new UtteranceLog();
        log.user = user;
        log.cardCodes = String.join(" ", cardCodes);
        log.converted = converted;
        log.spoken = spoken;
        log.occurredAt = occurredAt;
        return log;
    }

    public Long getId() { return id; }
    public AacUser getUser() { return user; }
    public List<String> getCardCodes() { return Arrays.asList(cardCodes.split(" ")); }
    public boolean isConverted() { return converted; }
    public boolean isSpoken() { return spoken; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
}
