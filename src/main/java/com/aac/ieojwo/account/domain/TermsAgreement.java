package com.aac.ieojwo.account.domain;

import com.aac.ieojwo.common.domain.BaseTimeEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "terms_agreements",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_terms_account_type_version",
                columnNames = {"account_id", "terms_type", "terms_version"}
        )
)
public class TermsAgreement extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(name = "terms_type", nullable = false, length = 40)
    private TermsType termsType;

    @Column(name = "terms_version", nullable = false, length = 30)
    private String termsVersion;

    @Column(nullable = false)
    private boolean agreed;

    @Column
    private LocalDateTime agreedAt;

    protected TermsAgreement() {
    }

    private TermsAgreement(Account account, TermsType termsType, String termsVersion, boolean agreed) {
        this.account = account;
        this.termsType = termsType;
        this.termsVersion = termsVersion;
        this.agreed = agreed;
        this.agreedAt = agreed ? LocalDateTime.now() : null;
    }

    public static TermsAgreement create(
            Account account,
            TermsType termsType,
            String termsVersion,
            boolean agreed
    ) {
        return new TermsAgreement(account, termsType, termsVersion, agreed);
    }

    public Long getId() { return id; }
    public Account getAccount() { return account; }
    public TermsType getTermsType() { return termsType; }
    public String getTermsVersion() { return termsVersion; }
    public boolean isAgreed() { return agreed; }
    public LocalDateTime getAgreedAt() { return agreedAt; }
}
