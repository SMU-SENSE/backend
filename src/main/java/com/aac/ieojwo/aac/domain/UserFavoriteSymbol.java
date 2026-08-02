package com.aac.ieojwo.aac.domain;

import com.aac.ieojwo.common.domain.BaseTimeEntity;
import com.aac.ieojwo.symbol.domain.Symbol;
import com.aac.ieojwo.user.domain.AacUser;
import jakarta.persistence.*;

@Entity
@Table(
        name = "user_favorite_symbols",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_favorite_symbol", columnNames = {"user_id", "symbol_id"})
)
public class UserFavoriteSymbol extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AacUser user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "symbol_id", nullable = false)
    private Symbol symbol;

    protected UserFavoriteSymbol() {
    }

    private UserFavoriteSymbol(AacUser user, Symbol symbol) {
        this.user = user;
        this.symbol = symbol;
    }

    public static UserFavoriteSymbol create(AacUser user, Symbol symbol) {
        return new UserFavoriteSymbol(user, symbol);
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
}
