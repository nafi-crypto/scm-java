package com.acme.scm.identity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

@Configuration
public class IdentityConfiguration {
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    JwtEncoder jwtEncoder(
            @Value("${scm.security.private-key-location}") String privateKeyLocation,
            @Value("${scm.security.public-key-location}") String publicKeyLocation) throws IOException {
        RSAPrivateKey privateKey;
        RSAPublicKey publicKey;
        try (var privateStream = Files.newInputStream(Path.of(privateKeyLocation));
             var publicStream = Files.newInputStream(Path.of(publicKeyLocation))) {
            privateKey = RsaKeyConverters.pkcs8().convert(privateStream);
            publicKey = RsaKeyConverters.x509().convert(publicStream);
        }
        RSAKey rsaKey = new RSAKey.Builder(publicKey).privateKey(privateKey).keyID("scm-signing-key").build();
        return new NimbusJwtEncoder(new ImmutableJWKSet<SecurityContext>(new JWKSet(rsaKey)));
    }
}
