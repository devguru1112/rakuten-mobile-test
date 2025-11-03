package com.rakuten.mobile.server.web;

import com.rakuten.mobile.server.domain.Answer;
import com.rakuten.mobile.server.service.ResponseService;
import com.rakuten.mobile.server.web.dto.SubmitResponseReq;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * REST controller to handle survey responses.
 * Provides endpoints to list existing responses and accept new submissions.
 */
@RestController
@RequestMapping("/api/surveys/{surveyId}/responses")
public class ResponseController {

    private final ResponseService responses;

    public ResponseController(ResponseService responses) { this.responses = responses; }

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
    public Map<String, Object> submit(@PathVariable UUID surveyId, @RequestBody SubmitResponseReq req) {
        List<Answer> answers = new ArrayList<>();
        for (var a : req.answers()) {
            Answer an = new Answer();
            an.setQuestionId(a.questionId());
            an.setValueJson(a.value());
            answers.add(an);
        }
        UUID id = responses.submit(surveyId, req.respondentId(), answers);
        return Map.of("responseId", id);
    }
}
