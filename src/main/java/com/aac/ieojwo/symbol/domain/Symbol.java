package com.aac.ieojwo.symbol.domain;

import com.aac.ieojwo.common.domain.BaseTimeEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "symbols")
public class Symbol extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private SymbolCategory category;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(length = 500)
    private String imageUrl;

    @Column(nullable = false, length = 200)
    private String ttsText;

    @Column(nullable = false)
    private boolean emergency;

    @Column(nullable = false)
    private int displayOrder;

    @Column(nullable = false)
    private boolean active;

    protected Symbol() {
    }

    private Symbol(
            SymbolCategory category,
            String name,
            String imageUrl,
            String ttsText,
            boolean emergency,
            int displayOrder
    ) {
        this.category = category;
        this.name = name;
        this.imageUrl = imageUrl;
        this.ttsText = ttsText;
        this.emergency = emergency;
        this.displayOrder = displayOrder;
        this.active = true;
    }

    public static Symbol create(
            SymbolCategory category,
            String name,
            String imageUrl,
            String ttsText,
            boolean emergency,
            int displayOrder
    ) {
        return new Symbol(category, name, imageUrl, ttsText, emergency, displayOrder);
    }

    public Long getId() {
        return id;
    }

    public SymbolCategory getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getTtsText() {
        return ttsText;
    }

    public boolean isEmergency() {
        return emergency;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public boolean isActive() {
        return active;
    }
}
