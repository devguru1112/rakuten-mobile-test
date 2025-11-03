package com.rakuten.mobile.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
// Uncomment if/when you add @PreAuthorize on methods
// import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

/**
 * Central Spring Security configuration.
 *
 * Key points:
 * - Stateless API (JWT only), no HTTP session.
 * - CSRF disabled (we're not using browser sessions/forms).
 * - CORS allowed (tighten for prod).
 * - Our JwtAuthFilter runs BEFORE UsernamePasswordAuthenticationFilter.
 * - Protects all /api/** endpoints; leaves actuator and Swagger open for dev.
 */
@Configuration
@EnableWebSecurity
// @EnableMethodSecurity // enable if you add @PreAuthorize on service/controller methods
public class SecurityConfig {

    /**
     * Main security filter chain.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
        return http
                // Most APIs need CORS; customize origins/headers for your environment
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // We rely on JWT, so disable CSRF and sessions
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Authorization rules:
                // - Allow actuator & swagger (dev).
                // - Allow GET "/" health/info (optional).
                // - Everything else requires authentication.
                .authorizeHttpRequests(reg -> reg
                        .requestMatchers("/actuator/**", "/v3/api-docs/**", "/swagger-ui/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/").permitAll()
                        .anyRequest().authenticated()
                )

                // Install our JWT filter before the default credentials filter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                // HTTP Basic can be left enabled/disabled; we don't use it but harmless in dev
                .httpBasic(Customizer.withDefaults())

                .build();
    }

    /**
     * Simple permissive CORS for local/dev.
     * Tighten this for production (specific origins, headers, and methods).
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(List.of("*")); // e.g. List.of("https://your-frontend.example")
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Tenant-Id"));
        cfg.setExposedHeaders(List.of("Location"));
        cfg.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}
