package com.rakuten.mobile.server;

import com.rakuten.mobile.server.config.JwtAuthFilter;
import com.rakuten.mobile.server.service.SurveyService;
import com.rakuten.mobile.server.web.SurveyController;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * SecurityWebMvcTest tests the security-related aspects of SurveyController.
 * It mocks the SurveyService and tests the behavior of the SurveyController
 * when the tenant ID in the JWT token is mismatched with the 'X-Tenant-Id' header.
 */
@WebMvcTest(controllers = SurveyController.class)
@Import({com.rakuten.mobile.server.config.SecurityConfig.class, JwtAuthFilter.class})
class SecurityWebMvcTest {

    @Autowired MockMvc mvc;

    @Mock SurveyService surveyService; // controller dependency

    private final String secret = "change-me-in-prod";
    private final String issuer = "survey-app";

    /**
     * Tests that a FORBIDDEN (403) response is returned when the tenant ID in the JWT
     * does not match the X-Tenant-Id header.
     */
    @Test
    void forbiddenWhenTenantHeaderDoesNotMatchTokenClaim() throws Exception {
        String jwt = TestJwt.hmacToken(secret, issuer, "user", "11111111-1111-1111-1111-111111111111", java.util.List.of("TENANT_ADMIN"));

        mvc.perform(get("/api/surveys")
                        .header("Authorization", "Bearer " + jwt)
                        .header("X-Tenant-Id", "22222222-2222-2222-2222-222222222222"))
                .andExpect(status().isForbidden());
    }

    /**
     * Tests that an OK (200) response is returned when the tenant ID in the JWT
     * matches the X-Tenant-Id header.
     */
    @Test
    void okWhenTenantMatches() throws Exception {
        String tenant = "11111111-1111-1111-1111-111111111111";
        String jwt = TestJwt.hmacToken(secret, issuer, "user", tenant, java.util.List.of("TENANT_ADMIN"));

        when(surveyService.list(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(org.springframework.data.domain.Page.empty());

        mvc.perform(get("/api/surveys")
                        .header("Authorization", "Bearer " + jwt)
                        .header("X-Tenant-Id", tenant))
                .andExpect(status().isOk());
    }
}

