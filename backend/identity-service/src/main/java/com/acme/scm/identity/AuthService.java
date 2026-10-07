package com.acme.scm.identity;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
    private static final long ACCESS_TOKEN_SECONDS = 900;
    private static final long REFRESH_TOKEN_SECONDS = 604800;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserAccountRepository users;
    private final RefreshTokenRepository refreshTokens;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final String issuer;

    public AuthService(
            UserAccountRepository users,
            RefreshTokenRepository refreshTokens,
            PasswordEncoder passwordEncoder,
            JwtEncoder jwtEncoder,
            @Value("${scm.security.issuer:http://identity-service:8081}") String issuer) {
        this.users = users;
        this.refreshTokens = refreshTokens;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        this.issuer = issuer;
    }

    @Transactional
    public TokenResponse register(String email, String password) {
        String normalizedEmail = email.trim().toLowerCase();
        ensureEmailAvailable(normalizedEmail);
        UserAccount user = users.save(new UserAccount(
                normalizedEmail, passwordEncoder.encode(password), UserRole.SUPPLIER));
        return issue(user);
    }

    @Transactional
    public AuthController.AdminUserView createUser(String email, String password, UserRole role) {
        String normalizedEmail = email.trim().toLowerCase();
        ensureEmailAvailable(normalizedEmail);
        UserAccount user = users.save(new UserAccount(
                normalizedEmail, passwordEncoder.encode(password), role));
        return new AuthController.AdminUserView(user.getId(), user.getEmail(), user.getRole());
    }

    @Transactional
    public TokenResponse login(String email, String password) {
        UserAccount user = users.findByEmailIgnoreCase(email.trim())
                .filter(UserAccount::isEnabled)
                .filter(account -> passwordEncoder.matches(password, account.getPasswordHash()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        return issue(user);
    }

    @Transactional
    public TokenResponse refresh(String rawRefreshToken) {
        RefreshToken token = refreshTokens.findByTokenHashForUpdate(hash(rawRefreshToken))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));
        if (token.isRevoked() || !token.getExpiresAt().isAfter(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token is expired or already used");
        }
        token.revoke();
        if (!token.getUser().isEnabled()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account is disabled");
        }
        return issue(token.getUser());
    }

    private TokenResponse issue(UserAccount user) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(ACCESS_TOKEN_SECONDS))
                .subject(user.getEmail())
                .id(UUID.randomUUID().toString())
                .claim("roles", List.of(user.getRole().name()))
                .claim("uid", user.getId().toString())
                .build();
        String accessToken = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
        byte[] secret = new byte[48];
        RANDOM.nextBytes(secret);
        String refreshToken = Base64.getUrlEncoder().withoutPadding().encodeToString(secret);
        refreshTokens.save(new RefreshToken(user, hash(refreshToken), now.plusSeconds(REFRESH_TOKEN_SECONDS)));
        return new TokenResponse(accessToken, refreshToken, "Bearer", ACCESS_TOKEN_SECONDS,
                List.of(user.getRole().name()));
    }

    private static String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (java.security.NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private void ensureEmailAvailable(String email) {
        if (users.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account already exists for this email");
        }
    }

    public record TokenResponse(String accessToken, String refreshToken, String tokenType,
                                long expiresIn, List<String> roles) { }
}
