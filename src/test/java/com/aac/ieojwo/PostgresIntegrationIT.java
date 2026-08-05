package com.aac.ieojwo;

import com.aac.ieojwo.aac.domain.SymbolUsageLog;
import com.aac.ieojwo.aac.domain.UsageAction;
import com.aac.ieojwo.aac.domain.UserFavoriteSymbol;
import com.aac.ieojwo.aac.repository.SymbolUsageLogRepository;
import com.aac.ieojwo.aac.repository.UserFavoriteSymbolRepository;
import com.aac.ieojwo.account.domain.Account;
import com.aac.ieojwo.account.domain.OAuthAccount;
import com.aac.ieojwo.account.domain.OAuthProvider;
import com.aac.ieojwo.account.repository.AccountRepository;
import com.aac.ieojwo.account.repository.OAuthAccountRepository;
import com.aac.ieojwo.guardian.domain.Guardian;
import com.aac.ieojwo.guardian.domain.GuardianRole;
import com.aac.ieojwo.guardian.domain.UserGuardian;
import com.aac.ieojwo.guardian.repository.GuardianRepository;
import com.aac.ieojwo.guardian.repository.UserGuardianRepository;
import com.aac.ieojwo.symbol.domain.Symbol;
import com.aac.ieojwo.symbol.domain.SymbolCategory;
import com.aac.ieojwo.symbol.repository.SymbolCategoryRepository;
import com.aac.ieojwo.symbol.repository.SymbolRepository;
import com.aac.ieojwo.user.domain.AacUser;
import com.aac.ieojwo.user.domain.GridSize;
import com.aac.ieojwo.user.domain.UserMode;
import com.aac.ieojwo.user.repository.AacUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("postgres")
@Testcontainers
class PostgresIntegrationIT {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16.14-alpine")
                    .withDatabaseName("malmoa_it")
                    .withUsername("malmoa_it")
                    .withPassword("integration-test-only");

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.security.oauth2.client.registration.google.client-id",
                () -> "postgres-it-login-disabled");
        registry.add("spring.security.oauth2.client.registration.google.client-secret",
                () -> "postgres-it-login-disabled");
        registry.add("server.servlet.session.cookie.secure", () -> false);
        registry.add("springdoc.api-docs.enabled", () -> true);
        registry.add("springdoc.swagger-ui.enabled", () -> true);
    }

    @Autowired AccountRepository accountRepository;
    @Autowired OAuthAccountRepository oauthAccountRepository;
    @Autowired GuardianRepository guardianRepository;
    @Autowired AacUserRepository aacUserRepository;
    @Autowired UserGuardianRepository userGuardianRepository;
    @Autowired SymbolCategoryRepository categoryRepository;
    @Autowired SymbolRepository symbolRepository;
    @Autowired UserFavoriteSymbolRepository favoriteRepository;
    @Autowired SymbolUsageLogRepository usageLogRepository;
    @Autowired TransactionTemplate transactionTemplate;
    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired MockMvc mockMvc;

    @Test
    void flywayHibernateRepositoriesConstraintsAndPublicApisWorkOnPostgres() throws Exception {
        assertThat(jdbcTemplate.queryForObject(
                "select count(*) from flyway_schema_history where success", Integer.class)).isEqualTo(4);

        String suffix = UUID.randomUUID().toString();
        Account account = inTransaction(() -> accountRepository.save(
                Account.createSocialAccount("owner-" + suffix + "@example.com", "Owner", null)));
        OAuthAccount oauth = inTransaction(() -> oauthAccountRepository.save(
                OAuthAccount.create(account, OAuthProvider.GOOGLE, "subject-" + suffix,
                        account.getEmail())));
        assertThat(oauthAccountRepository.findByProviderAndProviderSubject(
                OAuthProvider.GOOGLE, oauth.getProviderSubject())).isPresent();

        Account secondAccount = inTransaction(() -> accountRepository.save(
                Account.createSocialAccount("second-" + suffix + "@example.com", "Second", null)));
        assertThatThrownBy(() -> inTransaction(() -> oauthAccountRepository.saveAndFlush(
                OAuthAccount.create(secondAccount, OAuthProvider.GOOGLE,
                        oauth.getProviderSubject(), secondAccount.getEmail()))))
                .isInstanceOf(DataIntegrityViolationException.class);

        Guardian guardian = inTransaction(() -> guardianRepository.save(
                Guardian.createForAccount(account, "Guardian", account.getEmail(), null)));
        assertThatThrownBy(() -> inTransaction(() -> guardianRepository.saveAndFlush(
                Guardian.createForAccount(account, "Duplicate", "duplicate-" + suffix + "@example.com", null))))
                .isInstanceOf(DataIntegrityViolationException.class);

        AacUser user = inTransaction(() -> aacUserRepository.save(
                AacUser.create("AAC User", UserMode.SIMPLE, GridSize.GRID_2X2)));
        inTransaction(() -> userGuardianRepository.save(
                UserGuardian.create(user, guardian, GuardianRole.PRIMARY, true)));
        assertThatThrownBy(() -> inTransaction(() -> userGuardianRepository.saveAndFlush(
                UserGuardian.create(user, guardian, GuardianRole.FAMILY, false))))
                .isInstanceOf(DataIntegrityViolationException.class);

        SymbolCategory category = inTransaction(() -> categoryRepository.save(
                SymbolCategory.create("IT_" + suffix.substring(0, 8), "Integration", 1)));
        Symbol symbol = inTransaction(() -> symbolRepository.save(
                Symbol.create(category, "Water", null, "Water", false, 1)));

        inTransaction(() -> favoriteRepository.save(UserFavoriteSymbol.create(user, symbol)));
        assertThatThrownBy(() -> inTransaction(() -> favoriteRepository.saveAndFlush(
                UserFavoriteSymbol.create(user, symbol))))
                .isInstanceOf(DataIntegrityViolationException.class);

        for (UsageAction action : UsageAction.values()) {
            inTransaction(() -> usageLogRepository.save(
                    SymbolUsageLog.create(user, symbol, action, LocalDateTime.now())));
        }
        List<String> actions = jdbcTemplate.queryForList(
                "select action from symbol_usage_logs where user_id = ?", String.class, user.getId());
        assertThat(actions).containsExactlyInAnyOrder("SELECT", "CANCEL", "SPEAK");

        assertThatThrownBy(() -> jdbcTemplate.update(
                "insert into user_guardians " +
                        "(user_id, guardian_id, role, primary_guardian, created_at, updated_at) " +
                        "values (?, ?, 'FAMILY', false, current_timestamp, current_timestamp)",
                Long.MAX_VALUE, Long.MAX_VALUE))
                .isInstanceOf(DataIntegrityViolationException.class);

        mockMvc.perform(get("/api/v1/health")).andExpect(status().isOk());
        mockMvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
    }

    private <T> T inTransaction(Supplier<T> supplier) {
        return transactionTemplate.execute(status -> supplier.get());
    }
}

