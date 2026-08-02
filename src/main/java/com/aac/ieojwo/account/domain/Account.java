package com.aac.ieojwo.account.domain;

import com.aac.ieojwo.common.domain.BaseTimeEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "accounts")
public class Account extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 190)
    private String email;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(length = 500)
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private AccountType accountType;

    @Column(nullable = false)
    private boolean onboardingCompleted;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AccountStatus status;

    protected Account() {
    }

    private Account(String email, String name, String profileImageUrl) {
        this.email = email;
        this.name = name;
        this.profileImageUrl = profileImageUrl;
        this.onboardingCompleted = false;
        this.status = AccountStatus.ACTIVE;
    }

    public static Account createSocialAccount(String email, String name, String profileImageUrl) {
        return new Account(email, name, profileImageUrl);
    }

    public void updateSocialProfile(String email, String name, String profileImageUrl) {
        this.email = email;
        this.name = name;
        this.profileImageUrl = profileImageUrl;
    }

    public void completeOnboarding(AccountType accountType) {
        this.accountType = accountType;
        this.onboardingCompleted = true;
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getName() { return name; }
    public String getProfileImageUrl() { return profileImageUrl; }
    public AccountType getAccountType() { return accountType; }
    public boolean isOnboardingCompleted() { return onboardingCompleted; }
    public AccountStatus getStatus() { return status; }
}
