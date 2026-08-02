package com.aac.ieojwo.symbol.domain;

import com.aac.ieojwo.common.domain.BaseTimeEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "symbol_categories")
public class SymbolCategory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false)
    private int displayOrder;

    @Column(nullable = false)
    private boolean active;

    protected SymbolCategory() {
    }

    private SymbolCategory(String code, String name, int displayOrder) {
        this.code = code;
        this.name = name;
        this.displayOrder = displayOrder;
        this.active = true;
    }

    public static SymbolCategory create(String code, String name, int displayOrder) {
        return new SymbolCategory(code, name, displayOrder);
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public boolean isActive() {
        return active;
    }
}
