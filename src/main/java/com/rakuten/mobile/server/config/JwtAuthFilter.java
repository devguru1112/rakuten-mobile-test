package com.rakuten.mobile.server.config;


import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.rakuten.mobile.server.tenancy.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


/**
 * JwtAuthFilter class is responsible for authenticating and authorizing users based on JWT tokens.
 * It checks if the JWT token is valid, ensures the tenant information matches between the token
 * and the request header, and sets the Spring Security context with user authorities.
 *
 * - Verifies Bearer JWT token from the Authorization header.
 * - Ensures that the 'tenant' claim in the JWT matches the 'X-Tenant-Id' request header.
 * - Extracts user roles from the JWT and sets them as authorities in the Spring Security context.
 * - Sets the tenant context to allow multi-tenancy support.
 * - Clears the tenant context after processing the request.
 */

@Profile("!dev")
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final Algorithm alg;  // Algorithm used for verifying the JWT signature
    private final String issuer; // The expected issuer of the JWT token

    /**
     * Constructor to initialize JwtAuthFilter with secret key and issuer value.
     *
     * @param secret The secret key used to sign and verify the JWT.
     * @param issuer The expected issuer of the JWT token.
     */
    public JwtAuthFilter(@Value("${app.security.jwt.secret}") String secret,
                         @Value("${app.security.jwt.issuer}") String issuer) {

        if (secret == null || secret.isBlank()) {
            // Fail fast with a clear message; avoids a vague bean-creation failure
            throw new IllegalStateException("Missing JWT secret. Set 'app.jwt.secret' in application.properties or as an environment variable.");
        }
        this.alg = Algorithm.HMAC256(secret); // Initialize the HMAC algorithm with the secret
        this.issuer = issuer; // Set the expected issuer
    }

    /**
     * Filters each HTTP request to authenticate and authorize based on the JWT token.
     *
     * @param req The HTTP request object.
     * @param res The HTTP response object.
     * @param chain The filter chain to continue processing the request.
     * @throws ServletException If an error occurs during filter processing.
     * @throws IOException If an I/O error occurs during filter processing.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String auth = req.getHeader("Authorization");

        try {
            if (auth != null && auth.startsWith("Bearer ")) {
                String token = auth.substring(7); // Extract the token from the Authorization header
                DecodedJWT jwt = JWT.require(alg).withIssuer(issuer).build().verify(token);

                String tenant = jwt.getClaim("tenant").asString();
                String headerTenant = req.getHeader("X-Tenant-Id");

                // If the tenant in the JWT does not match the tenant in the request header, return an error
                if (!Objects.equals(tenant, headerTenant)) {
                    res.sendError(HttpServletResponse.SC_FORBIDDEN, "Tenant mismatch");
                    return;
                }

                // Extract roles from the JWT claims, or use an empty list if roles are not present
                var roles = Optional.ofNullable(jwt.getClaim("roles").asList(String.class)).orElse(List.of());
                var authorities = roles.stream().map(r -> new SimpleGrantedAuthority("ROLE_" + r)).toList();

                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(jwt.getSubject(), null, authorities));

                TenantContext.set(tenant);
            }
            chain.doFilter(req, res);
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            TenantContext.clear();
            res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
        } finally {
            TenantContext.clear();
        }
    }
}