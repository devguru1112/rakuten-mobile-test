package com.rakuten.mobile.server.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rakuten.mobile.server.domain.Answer;
import com.rakuten.mobile.server.domain.Response;
import com.rakuten.mobile.server.repo.AnswerRepository;
import com.rakuten.mobile.server.repo.ResponseRepository;
import com.rakuten.mobile.server.service.ResponseService;
import com.rakuten.mobile.server.web.dto.SubmitResponseReq;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.io.IOException;
import java.util.*;

/**
 * REST controller to handle survey responses.
 * Provides endpoints to list existing responses and accept new submissions.
 */
@RestController
@RequestMapping("/api/surveys/{surveyId}/responses")
public class ResponseController {

    private final ResponseService responses;
    private final ResponseRepository rRepo;
    private final AnswerRepository aRepo;
    private final ObjectMapper om = new ObjectMapper();

    public ResponseController(ResponseService responses,
                              ResponseRepository rRepo,
                              AnswerRepository aRepo) {
        this.responses = responses;
        this.rRepo = rRepo;
        this.aRepo = aRepo;
    }
    /**
     * Endpoint to list responses for a specific survey with pagination.
     *
     * @param surveyId The ID of the survey to list responses for.
     * @param pageable Pagination information (e.g., page number, page size).
     * @return A page of response data (minimal details such as response ID, submission timestamp, and respondent ID).
     */
    @GetMapping
    public Page<Map<String, Object>> list(@PathVariable UUID surveyId, Pageable pageable) {
        return responses.list(surveyId, pageable)
                .map(r -> Map.of(
                        "id", r.getId(),
                        "submittedAt", r.getSubmittedAt(),
                        "respondentId", r.getRespondentId()
                ));
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
            @RequestHeader(name = "Idempotency-Key", required = false) String idemKey) {
        List<Answer> answers = new ArrayList<>();
        for (var a : req.answers()) {
            Answer an = new Answer();
            an.setQuestionId(a.questionId());
            an.setValueJson(a.value());
            answers.add(an);
        }
        UUID id = responses.submit(surveyId, req.respondentId(), answers, idemKey);
        return Map.of("responseId", id);
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
        List<Response> rs = rRepo.findBySurveyId(surveyId, Pageable.unpaged()).getContent();

        if ("json".equalsIgnoreCase(format)) {
            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
            var out = new ArrayList<Map<String, Object>>();
            for (var r : rs) {
                var answers = aRepo.findByResponseId(r.getId()).stream()
                        .map(a -> Map.of("questionId", a.getQuestionId(), "valueJson", a.getValueJson()))
                        .toList();
                out.add(Map.of(
                        "responseId", r.getId(),
                        "submittedAt", r.getSubmittedAt(),
                        "respondentId", r.getRespondentId(),
                        "answers", answers));
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
            var answers = aRepo.findByResponseId(r.getId());
            for (var a : answers) {
                // value_json may contain commas—quote as JSON string; CSV parsers can handle quoted fields
                String value = om.writeValueAsString(a.getValueJson());
                writer.printf("%s,%s,%s,%s,%s%n",
                        r.getId(), r.getSubmittedAt(), r.getRespondentId(), a.getQuestionId(), value);
            }
        }
        writer.flush();
    }


}
