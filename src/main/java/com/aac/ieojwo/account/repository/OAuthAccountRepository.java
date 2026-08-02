package com.aac.ieojwo.account.repository;

import com.aac.ieojwo.account.domain.OAuthAccount;
import com.aac.ieojwo.account.domain.OAuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OAuthAccountRepository extends JpaRepository<OAuthAccount, Long> {
    Optional<OAuthAccount> findByProviderAndProviderSubject(OAuthProvider provider, String providerSubject);
}
