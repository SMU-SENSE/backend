package com.aac.ieojwo.notification.domain;

import com.aac.ieojwo.common.domain.BaseTimeEntity;
import com.aac.ieojwo.guardian.domain.Guardian;
import com.aac.ieojwo.symbol.domain.Symbol;
import com.aac.ieojwo.user.domain.AacUser;
import jakarta.persistence.*;

@Entity
@Table(name = "guardian_notifications", indexes = {
        @Index(name = "idx_guardian_notification_guardian", columnList = "guardian_id, created_at")
})
public class GuardianNotification extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "guardian_id", nullable = false)
    private Guardian guardian;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "aac_user_id", nullable = false)
    private AacUser aacUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "symbol_id")
    private Symbol symbol;

    @Column(nullable = false, length = 200)
    private String message;

    @Column(nullable = false)
    private boolean read;

    protected GuardianNotification() {
    }

    private GuardianNotification(Guardian guardian, AacUser aacUser, Symbol symbol, String message) {
        this.guardian = guardian;
        this.aacUser = aacUser;
        this.symbol = symbol;
        this.message = message;
        this.read = false;
    }

    public static GuardianNotification emergencySymbolUsed(Guardian guardian, AacUser aacUser, Symbol symbol) {
        return new GuardianNotification(guardian, aacUser, symbol,
                aacUser.getName() + "님이 긴급 상징 \"" + symbol.getName() + "\"을(를) 사용했습니다.");
    }

    public void markRead() {
        this.read = true;
    }

    public Long getId() {
        return id;
    }

    public Guardian getGuardian() {
        return guardian;
    }

    public AacUser getAacUser() {
        return aacUser;
    }

    public Symbol getSymbol() {
        return symbol;
    }

    public String getMessage() {
        return message;
    }

    public boolean isRead() {
        return read;
    }
}
