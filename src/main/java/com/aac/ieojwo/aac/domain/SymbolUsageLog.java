package com.aac.ieojwo.aac.domain;

import com.aac.ieojwo.symbol.domain.Symbol;
import com.aac.ieojwo.user.domain.AacUser;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "symbol_usage_logs", indexes = {
        @Index(name = "idx_usage_user_time", columnList = "user_id, occurred_at"),
        @Index(name = "idx_usage_symbol", columnList = "symbol_id")
})
public class SymbolUsageLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AacUser user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "symbol_id", nullable = false)
    private Symbol symbol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UsageAction action;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    protected SymbolUsageLog() {
    }

    private SymbolUsageLog(AacUser user, Symbol symbol, UsageAction action, LocalDateTime occurredAt) {
        this.user = user;
        this.symbol = symbol;
        this.action = action;
        this.occurredAt = occurredAt;
    }

    public static SymbolUsageLog create(
            AacUser user,
            Symbol symbol,
            UsageAction action,
            LocalDateTime occurredAt
    ) {
        return new SymbolUsageLog(user, symbol, action, occurredAt);
    }

    public Long getId() {
        return id;
    }

    public AacUser getUser() {
        return user;
    }

    public Symbol getSymbol() {
        return symbol;
    }

    public UsageAction getAction() {
        return action;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }
}
