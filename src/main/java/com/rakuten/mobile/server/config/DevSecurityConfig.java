package com.rakuten.mobile.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Profile("dev")
public class DevSecurityConfig {

    /**
     * Dev profile: disable auth entirely so all endpoints are open.
     */
    @Bean
    SecurityFilterChain devSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                // Allow everything during local dev
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/**", "/h2-console/**").permitAll()
                        .anyRequest().permitAll()
                )
                // H2 console needs frames
                .headers(h -> h.frameOptions(f -> f.disable()))
                .httpBasic(Customizer.withDefaults()) // optional
                .formLogin(fl -> fl.disable())
                .logout(lo -> lo.disable());

        return http.build();
    }
}