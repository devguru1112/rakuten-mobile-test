package com.rakuten.mobile.server.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rakuten.mobile.server.domain.Response;
import com.rakuten.mobile.server.repo.ResponseRepository;
import com.rakuten.mobile.server.service.ResponseService;
import com.rakuten.mobile.server.web.dto.ResponseRes;
import com.rakuten.mobile.server.web.dto.SubmitResponseReq;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller to handle survey responses.
 * Provides endpoints to list existing responses and accept new submissions.
 */
@RestController
@RequestMapping("/api/surveys/{surveyId}/responses")
public class ResponseController {

    private final ResponseService responses;
    private final ResponseRepository rRepo;
    private final ObjectMapper om = new ObjectMapper();

    public ResponseController(ResponseService responses,
                              ResponseRepository rRepo) {
        this.responses = responses;
        this.rRepo = rRepo;
    }
    /**
     * Endpoint to list responses for a specific survey with pagination.
     *
     * @param surveyId The ID of the survey to list responses for.
     * @param pageable Pagination information (e.g., page number, page size).
     * @return A page of response data (minimal details such as response ID, submission timestamp, and respondent ID).
     */
    @GetMapping
    public Page<ResponseRes> list(@PathVariable UUID surveyId, Pageable pageable) {
        return responses.list(surveyId, pageable).map(ResponseRes::from);
    }

    /**
     * Endpoint to submit a new response for a survey.
     *
     * @param surveyId The ID of the survey to submit the response for.
     * @param req The request body containing the answers and the respondent's ID.
     * @return A Map with the ID of the newly created response.
     */
    @PostMapping
    public Map<String, Object> submit(
            @PathVariable UUID surveyId,
            @RequestBody SubmitResponseReq req,
            @RequestHeader(name = "X-Tenant-Id", required = false) UUID tenantId,
            @RequestHeader(name = "Idempotency-Key", required = false) String idemKey
            ) {

        if (tenantId == null) {
            tenantId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        }

        Map<String, Object> answers = req.answers().stream()
                .filter(a -> a.value() != null)
                .collect(Collectors.toMap(a -> a.questionId().toString(), (SubmitResponseReq.Ans::value)));


        UUID id = responses.submit(surveyId, tenantId, req.respondentId(), answers, idemKey);
        return Map.of("responseId", id);
    }

    /**
     * Endpoint to retrieve a specific survey by its ID.
     *
     * @param id The ID of the survey to retrieve.
     * @return The requested survey as a SurveyRes DTO.
     * @throws IllegalArgumentException If the survey is not found.
     */
    @GetMapping("/{id}")
    public ResponseRes get(@PathVariable UUID id) {
        Response response = responses.get(id).orElseThrow(() -> new IllegalArgumentException("Survey not found"));
        return ResponseRes.from(response);
    }

    /**
     * Export responses:
     *  - CSV (default) produces one row per answer (responseId, questionId, valueJson).
     *  - JSON returns an array: [{ responseId, submittedAt, respondentId, answers:[{questionId,valueJson}] }]
     */
    @GetMapping("/export")
    public void export(@PathVariable UUID surveyId,
                       @RequestParam(defaultValue = "csv") String format,
                       HttpServletResponse res) throws IOException {
        List<Response> rs = rRepo.findAllBySurveyId(surveyId);

        if ("json".equalsIgnoreCase(format)) {
            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
            var out = new ArrayList<Map<String, Object>>();
            for (var r : rs) {
                out.add(Map.of(
                        "responseId", r.getId(),
                        "submittedAt", r.getSubmittedAt(),
                        "respondentId", r.getRespondentId(),
                        "answers", r.getAnswersJson()));
            }
            om.writeValue(res.getOutputStream(), out);
            return;
        }

        // CSV export
        res.setContentType("text/csv");
        res.setHeader("Content-Disposition", "attachment; filename=\"survey-" + surveyId + "-export.csv\"");
        var writer = res.getWriter();
        writer.println("response_id,submitted_at,respondent_id,question_id,value_json");
        for (var r : rs) {
            var answers = r.getAnswersJson();
            for (Map.Entry<String, Object> entry : answers.entrySet()) {
                // value_json may contain commas—quote as JSON string; CSV parsers can handle quoted fields
                String value = om.writeValueAsString(entry.getValue());
                writer.printf("%s,%s,%s,%s,%s%n",
                        r.getId().toString(), r.getSubmittedAt().toString(), r.getRespondentId().toString(), entry.getKey(), value);
            }
        }
        writer.flush();
    }



}
