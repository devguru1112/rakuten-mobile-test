package com.rakuten.mobile.server.tenancy;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.hibernate.Session;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

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
public class TenantHibernateFilterEnabler extends OncePerRequestFilter {

    @PersistenceContext
    private EntityManager em; // EntityManager to interact with the persistence context

    /**
     * This method enables the tenant filter for each Hibernate session, applying the tenant ID
     * from the TenantContext to the session.
     *
     * @param req The HTTP request object.
     * @param res The HTTP response object.
     * @param chain The filter chain to continue processing the request.
     * @throws ServletException If an error occurs during filter processing.
     * @throws java.io.IOException If an I/O error occurs during filter processing.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, java.io.IOException {

        // Unwrap the Hibernate session from the EntityManager
        Session session = em.unwrap(Session.class);

        // Enable the tenant filter and set the tenant ID from TenantContext
        session.enableFilter("tenantFilter").setParameter("tenantId", TenantContext.required());

        try {
            // Continue with the filter chain to process the request
            chain.doFilter(req, res);
        } finally {
            // Disable the tenant filter after the request is processed
            session.disableFilter("tenantFilter");
        }
    }
}
