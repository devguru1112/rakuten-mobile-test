package com.rakuten.mobile.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest
@AutoConfigureMockMvc
class IntegrationFlowTest {

    static PostgreSQLContainer<?> pg = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("survey").withUsername("survey").withPassword("survey");

    @DynamicPropertySource
    static void dbProps(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", pg::getJdbcUrl);
        r.add("spring.datasource.username", pg::getUsername);
        r.add("spring.datasource.password", pg::getPassword);
    }

    @BeforeAll static void start() { pg.start(); }
    @AfterAll  static void stop()  { pg.stop(); }

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    String tenant = "11111111-1111-1111-1111-111111111111";
    String secret = "change-me-in-prod";
    String issuer = "survey-app";

    private String jwt() {
        return TestJwt.hmacToken(secret, issuer, "user", tenant, java.util.List.of("TENANT_ADMIN"));
    }

    @Test
    void fullFlow_createPublishListExport() throws Exception {
        var token = jwt();

        // 1) Create survey
        var createRes = mvc.perform(post("/api/surveys")
                        .header("Authorization", "Bearer " + token)
                        .header("X-Tenant-Id", tenant)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Engagement Q4\"}"))
                .andReturn().getResponse();

        assertThat(createRes.getStatus()).isEqualTo(200);
        JsonNode created = om.readTree(createRes.getContentAsString());
        String surveyId = created.get("id").asText();

        // 2) Add questions (replace)
        String body = """
      [
        {"type":"SINGLE_CHOICE","text":"How satisfied are you?","required":true,"position":1,
         "options":[{"text":"Very","position":1},{"text":"Somewhat","position":2},{"text":"Not","position":3}]},
        {"type":"TEXT","text":"What can we improve?","required":false,"position":2}
      ]
      """;
        var qRes = mvc.perform(post("/api/surveys/"+surveyId+"/questions")
                        .header("Authorization", "Bearer " + token)
                        .header("X-Tenant-Id", tenant)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andReturn().getResponse();
        assertThat(qRes.getStatus()).isEqualTo(200);

        // 3) Publish
        var pubRes = mvc.perform(patch("/api/surveys/"+surveyId+"/publish")
                        .header("Authorization", "Bearer " + token)
                        .header("X-Tenant-Id", tenant))
                .andReturn().getResponse();
        assertThat(pubRes.getStatus()).isEqualTo(200);

        // 4) List surveys
        var listRes = mvc.perform(get("/api/surveys")
                        .header("Authorization", "Bearer " + token)
                        .header("X-Tenant-Id", tenant))
                .andReturn().getResponse();
        assertThat(listRes.getStatus()).isEqualTo(200);

        // 5a) Export JSON (no responses yet, but endpoint should work)
        var exportRes = mvc.perform(get("/api/surveys/"+surveyId+"/responses/export?format=json")
                        .header("Authorization", "Bearer " + token)
                        .header("X-Tenant-Id", tenant))
                .andReturn().getResponse();
        assertThat(exportRes.getStatus()).isEqualTo(200);
        assertThat(exportRes.getContentType()).contains("application/json");

        // 5b) Export CSV stream (just verify headers and 200)
        var streamRes = mvc.perform(get("/api/surveys/"+surveyId+"/responses/export/stream?format=csv")
                        .header("Authorization", "Bearer " + token)
                        .header("X-Tenant-Id", tenant))
                .andReturn().getResponse();
        assertThat(streamRes.getStatus()).isEqualTo(200);
        assertThat(streamRes.getHeader("Content-Disposition")).contains("attachment; filename=\"survey-" + surveyId + "-stream.csv\"");
    }
}
