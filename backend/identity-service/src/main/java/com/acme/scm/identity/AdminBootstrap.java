package com.acme.scm.identity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdminBootstrap implements ApplicationRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdminBootstrap.class);

    private final UserAccountRepository users;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String password;

    public AdminBootstrap(
            UserAccountRepository users,
            PasswordEncoder passwordEncoder,
            @Value("${SCM_ADMIN_EMAIL:}") String email,
            @Value("${SCM_ADMIN_PASSWORD:}") String password) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.email = email;
        this.password = password;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (email.isBlank() && password.isBlank()) {
            LOGGER.warn("No bootstrap admin configured. Set SCM_ADMIN_EMAIL and SCM_ADMIN_PASSWORD to provision an administrator.");
            return;
        }
        if (email.isBlank() || password.length() < 16) {
            throw new IllegalStateException("Bootstrap admin requires an email and a password of at least 16 characters");
        }
        String normalizedEmail = email.trim().toLowerCase();
        UserAccount account = users.findByEmailIgnoreCase(normalizedEmail).orElseGet(() ->
                users.save(new UserAccount(normalizedEmail, passwordEncoder.encode(password), UserRole.ADMIN)));
        if (account.getRole() != UserRole.ADMIN) {
            throw new IllegalStateException("Bootstrap admin email is already assigned to a non-admin account");
        }
    }
}
