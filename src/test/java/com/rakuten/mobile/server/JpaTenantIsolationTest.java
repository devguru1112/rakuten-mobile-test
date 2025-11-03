package com.rakuten.mobile.server;

import com.rakuten.mobile.server.domain.Survey;
import com.rakuten.mobile.server.repo.SurveyRepository;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that when the Hibernate filter is enabled with tenant A,
 * queries do not return rows from tenant B.
 */
@DataJpaTest
class JpaTenantIsolationTest {

    @Autowired EntityManager em;
    @Autowired SurveyRepository surveys;

    @Test
    void queriesAreScopedByTenantFilter() {
        // seed two tenants
        UUID tenantA = UUID.randomUUID();
        UUID tenantB = UUID.randomUUID();

        persistSurvey(tenantA, "A1");
        persistSurvey(tenantB, "B1");
        persistSurvey(tenantA, "A2");

        // enable filter for tenant A
        Session session = em.unwrap(Session.class);
        session.enableFilter("tenantFilter").setParameter("tenantId", tenantA.toString());

        List<Survey> visible = surveys.findAll();
        assertThat(visible).extracting(Survey::getTitle).containsExactlyInAnyOrder("A1", "A2");

        // switch to tenant B
        session.disableFilter("tenantFilter");
        session.enableFilter("tenantFilter").setParameter("tenantId", tenantB.toString());

        visible = surveys.findAll();
        assertThat(visible).extracting(Survey::getTitle).containsExactly("B1");
    }

    private void persistSurvey(UUID tenant, String title) {
        var s = new Survey();
        s.setId(UUID.randomUUID());
        s.setTenantId(tenant);
        s.setTitle(title);
        em.persist(s);
        em.flush();
        em.clear();
    }
}
