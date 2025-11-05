package com.rakuten.mobile.server.tenancy;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Profile("dev")
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DevTenantStubFilter extends OncePerRequestFilter {

    // Use header if present; otherwise set a fixed dev tenant
    private static final UUID DEFAULT_DEV_TENANT =
            UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String hdr = request.getHeader("X-Tenant-Id");
            if (hdr != null && !hdr.isBlank()) {
                TenantContext.set(hdr);
            } else {
                TenantContext.set(DEFAULT_DEV_TENANT.toString());
            }
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
