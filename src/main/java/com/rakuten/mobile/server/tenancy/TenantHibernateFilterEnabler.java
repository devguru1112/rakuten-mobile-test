package com.rakuten.mobile.server.tenancy;

import jakarta.persistence.EntityManager;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * TenantHibernateFilterEnabler class is a filter that integrates with Hibernate's multi-tenancy
 * support. It ensures that a Hibernate filter for tenant-based filtering is enabled for each
 * request, and that the appropriate tenant ID is applied to the session.
 *
 * - The filter intercepts each request and applies the tenant context to the Hibernate session.
 * - The tenant ID is retrieved from the `TenantContext` and set as a parameter for the session filter.
 * - After the request is processed, the tenant filter is disabled to clean up.
 */

@Component
@RequiredArgsConstructor
public class TenantHibernateFilterEnabler extends OncePerRequestFilter {

    private final EntityManager entityManager; // EntityManager to interact with the persistence context

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String tenantId = TenantContext.get(); // null if unauthenticated/public
        Session session = entityManager.unwrap(Session.class);
        Filter filter = null;

        try {
            if (tenantId != null) {
                filter = session.enableFilter("tenantFilter");
                filter.setParameter("tenantId", tenantId);
            }
            chain.doFilter(request, response);
        } finally {
            if (filter != null) {
                session.disableFilter("tenantFilter");
            }
        }
    }
}
