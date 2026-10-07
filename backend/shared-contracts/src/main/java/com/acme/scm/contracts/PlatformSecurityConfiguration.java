package com.acme.scm.contracts;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.interfaces.RSAPublicKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity
public class PlatformSecurityConfiguration {
    @Bean
    SecurityFilterChain scmSecurityFilterChain(
            HttpSecurity http,
            @Value("${scm.security.public-endpoints:/actuator/health,/actuator/health/**,/actuator/info,/actuator/prometheus,/v3/api-docs/**,/swagger-ui/**}") String publicEndpoints)
            throws Exception {
        String[] permitted = Arrays.stream(publicEndpoints.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .toArray(String[]::new);

        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(permitted).permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(resourceServer -> resourceServer.jwt(jwt ->
                        jwt.jwtAuthenticationConverter(scmJwtAuthenticationConverter())))
                .build();
    }

    private Converter<Jwt, AbstractAuthenticationToken> scmJwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter scopes = new JwtGrantedAuthoritiesConverter();
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<GrantedAuthority> scopeAuthorities = scopes.convert(jwt);
            List<String> roles = jwt.getClaimAsStringList("roles");
            Stream<GrantedAuthority> roleAuthorities = (roles == null ? List.<String>of() : roles).stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role));
            return Stream.concat(scopeAuthorities == null ? Stream.empty() : scopeAuthorities.stream(), roleAuthorities)
                    .toList();
        });
        return converter;
    }

    @Bean
    JwtDecoder scmJwtDecoder(
            @Value("${scm.security.public-key-location}") String publicKeyLocation,
            @Value("${scm.security.issuer}") String issuer) throws IOException {
        RSAPublicKey publicKey;
        try (var publicStream = Files.newInputStream(Path.of(publicKeyLocation))) {
            publicKey = org.springframework.security.converter.RsaKeyConverters.x509().convert(publicStream);
        }
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(publicKey).build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuer));
        return decoder;
    }

    @Bean
    CorsConfigurationSource scmCorsConfigurationSource(
            @Value("${scm.security.allowed-origins:http://localhost:5173}") String allowedOrigins) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(allowedOrigins.split(",")));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Idempotency-Key", "X-Correlation-ID"));
        configuration.setExposedHeaders(List.of("Location", "X-Correlation-ID"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}
