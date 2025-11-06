package com.rakuten.mobile.server;

import com.rakuten.mobile.server.config.JwtAuthFilter;
import com.rakuten.mobile.server.service.ResponseService;
import com.rakuten.mobile.server.web.ResponseController;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ResponseController.class)
@Import({com.rakuten.mobile.server.config.SecurityConfig.class, JwtAuthFilter.class})
class IdempotencyWebMvcTest {

    @Autowired MockMvc mvc;

    @Mock ResponseService responseService;
    @Mock com.rakuten.mobile.server.repo.ResponseRepository rRepo;
    @Mock com.rakuten.mobile.server.repo.AnswerRepository aRepo;

    String tenant = "11111111-1111-1111-1111-111111111111";
    String jwt = TestJwt.hmacToken("change-me-in-prod","survey-app","user",tenant, List.of("TENANT_ADMIN"));

    @Test
    void reusingSameKeyReturnsSameResponseId() throws Exception {
        UUID respId = UUID.randomUUID();
        Mockito.when(responseService.submit(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.anyMap(), ArgumentMatchers.eq("abc123")))
                .thenReturn(respId);

        String payload = "{\"respondentId\":null,\"answers\":[]}";
        mvc.perform(post("/api/surveys/"+UUID.randomUUID()+"/responses")
                        .header("Authorization", "Bearer " + jwt)
                        .header("X-Tenant-Id", tenant)
                        .header("Idempotency-Key", "abc123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responseId").value(respId.toString()));
    }
}
