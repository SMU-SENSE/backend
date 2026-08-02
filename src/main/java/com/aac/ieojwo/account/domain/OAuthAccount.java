package com.aac.ieojwo.account.domain;

import com.aac.ieojwo.common.domain.BaseTimeEntity;
import jakarta.persistence.*;

@Entity
@Table(
        name = "oauth_accounts",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_oauth_provider_subject",
                columnNames = {"provider", "provider_subject"}
        )
)
public class OAuthAccount extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OAuthProvider provider;

    @Column(name = "provider_subject", nullable = false, length = 255)
    private String providerSubject;

    @Column(name = "provider_email", nullable = false, length = 190)
    private String providerEmail;

    protected OAuthAccount() {
    }

    private OAuthAccount(Account account, OAuthProvider provider, String providerSubject, String providerEmail) {
        this.account = account;
        this.provider = provider;
        this.providerSubject = providerSubject;
        this.providerEmail = providerEmail;
    }

    public static OAuthAccount create(
            Account account,
            OAuthProvider provider,
            String providerSubject,
            String providerEmail
    ) {
        return new OAuthAccount(account, provider, providerSubject, providerEmail);
    }

    public void updateProviderEmail(String providerEmail) {
        this.providerEmail = providerEmail;
    }

    public Long getId() { return id; }
    public Account getAccount() { return account; }
    public OAuthProvider getProvider() { return provider; }
    public String getProviderSubject() { return providerSubject; }
    public String getProviderEmail() { return providerEmail; }
}
