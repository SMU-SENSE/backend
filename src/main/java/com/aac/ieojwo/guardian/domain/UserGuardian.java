package com.aac.ieojwo.guardian.domain;

import com.aac.ieojwo.common.domain.BaseTimeEntity;
import com.aac.ieojwo.user.domain.AacUser;
import jakarta.persistence.*;

@Entity
@Table(
        name = "user_guardians",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_guardian", columnNames = {"user_id", "guardian_id"})
)
public class UserGuardian extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AacUser user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "guardian_id", nullable = false)
    private Guardian guardian;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GuardianRole role;

    @Column(nullable = false)
    private boolean primaryGuardian;

    protected UserGuardian() {
    }

    private UserGuardian(AacUser user, Guardian guardian, GuardianRole role, boolean primaryGuardian) {
        this.user = user;
        this.guardian = guardian;
        this.role = role;
        this.primaryGuardian = primaryGuardian;
    }

    public static UserGuardian create(AacUser user, Guardian guardian, GuardianRole role, boolean primaryGuardian) {
        return new UserGuardian(user, guardian, role, primaryGuardian);
    }

    public Long getId() {
        return id;
    }

    public AacUser getUser() {
        return user;
    }

    public Guardian getGuardian() {
        return guardian;
    }

    public GuardianRole getRole() {
        return role;
    }

    public boolean isPrimaryGuardian() {
        return primaryGuardian;
    }
}
