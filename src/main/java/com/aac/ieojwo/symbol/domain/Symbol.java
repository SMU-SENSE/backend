package com.aac.ieojwo.symbol.domain;

import com.aac.ieojwo.common.domain.BaseTimeEntity;
import com.aac.ieojwo.user.domain.AacUser;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "symbols", indexes = @Index(name = "idx_symbols_owner_user", columnList = "owner_user_id"))
public class Symbol extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** model.json 이 쓰는 카드 ID (예: ic_food_water). 한번 정하면 바꾸거나 재사용하지 않는다. */
    @Column(name = "card_code", unique = true, length = 100)
    private String cardCode;

    /** 커스텀 카드를 만든 AAC 사용자. 기본 카드는 null. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id")
    private AacUser owner;

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

    public static Symbol createCard(String cardCode, SymbolCategory category, String label,
                                    String imageUrl, boolean emergency, int displayOrder) {
        Symbol symbol = new Symbol(category, label, imageUrl, label, emergency, displayOrder);
        symbol.cardCode = cardCode;
        return symbol;
    }

    public static Symbol createCustom(AacUser owner, SymbolCategory category, String label,
                                      String imageUrl, String ttsText, boolean emergency) {
        Symbol symbol = new Symbol(category, label, imageUrl, ttsText, emergency, 0);
        symbol.cardCode = "ic_custom_" + UUID.randomUUID().toString().replace("-", "");
        symbol.owner = owner;
        return symbol;
    }

    /** 사용 기록의 ID가 깨지지 않도록 행은 지우지 않고 비활성화만 한다. */
    public void deactivate() {
        this.active = false;
    }

    public boolean isCustom() {
        return owner != null;
    }

    /** 다른 사용자의 커스텀 카드는 보이지 않는다. 기본 카드는 모두에게 보인다. */
    public boolean isUsableBy(AacUser user) {
        return owner == null || owner.getId().equals(user.getId());
    }

    /** 추천 후보 여부. 긴급어는 추천에서 뺀다(model.json 에도 없음). */
    public boolean isPredict() {
        return !emergency;
    }

    public String getCardCode() {
        return cardCode;
    }

    public AacUser getOwner() {
        return owner;
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
