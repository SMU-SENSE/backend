package com.aac.ieojwo.guardian.domain;

import com.aac.ieojwo.account.domain.Account;
import com.aac.ieojwo.common.domain.BaseTimeEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "guardians")
public class Guardian extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Column(length = 30)
    private String phone;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", unique = true)
    private Account account;

    protected Guardian() {
    }

    private Guardian(String name, String email, String phone) {
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    private Guardian(Account account, String name, String email, String phone) {
        this.account = account;
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    public static Guardian create(String name, String email, String phone) {
        return new Guardian(name, email, phone);
    }

    public static Guardian createForAccount(
            Account account,
            String name,
            String email,
            String phone
    ) {
        return new Guardian(account, name, email, phone);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public void linkAccount(Account account) {
        this.account = account;
    }

    public Account getAccount() {
        return account;
    }
}
