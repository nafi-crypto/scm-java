package com.acme.scm.gateway;

import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Mono;

@Configuration
public class GatewayConfiguration {
    private static final Pattern SAFE_CORRELATION_ID = Pattern.compile("[A-Za-z0-9._-]{1,100}");

    @Bean
    KeyResolver clientAddressKeyResolver() {
        return exchange -> Mono.justOrEmpty(exchange.getRequest().getRemoteAddress())
                .map(address -> address.getAddress().getHostAddress())
                .switchIfEmpty(Mono.error(new IllegalStateException("Client address is unavailable for rate limiting")));
    }

    @Bean
    GlobalFilter correlationIdFilter() {
        return (exchange, chain) -> {
            String supplied = exchange.getRequest().getHeaders().getFirst("X-Correlation-ID");
            String correlationId = supplied != null && SAFE_CORRELATION_ID.matcher(supplied).matches()
                    ? supplied : UUID.randomUUID().toString();
            var request = exchange.getRequest().mutate()
                    .header("X-Correlation-ID", correlationId)
                    .build();
            exchange.getResponse().getHeaders().set("X-Correlation-ID", correlationId);
            return chain.filter(exchange.mutate().request(request).build());
        };
    }

    @Bean
    CorsWebFilter gatewayCorsWebFilter(
            @Value("${scm.security.allowed-origins:http://localhost:5173}") String allowedOrigins) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(allowedOrigins.split(",")));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of(
                HttpHeaders.AUTHORIZATION, HttpHeaders.CONTENT_TYPE, "Idempotency-Key", "X-Correlation-ID"));
        configuration.setExposedHeaders(List.of("Location", "X-Correlation-ID"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return new CorsWebFilter(source);
    }
}
