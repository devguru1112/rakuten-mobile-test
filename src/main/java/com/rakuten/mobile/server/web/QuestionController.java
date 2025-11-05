package com.rakuten.mobile.server.web;

import com.rakuten.mobile.server.domain.OptionChoice;
import com.rakuten.mobile.server.domain.Question;
import com.rakuten.mobile.server.service.QuestionService;
import com.rakuten.mobile.server.web.dto.question.CreateQuestionReq;
import com.rakuten.mobile.server.web.dto.question.QuestionRes;
import com.rakuten.mobile.server.web.dto.question.ReplaceQuestionsReq;
import com.rakuten.mobile.server.web.dto.question.UpdateQuestionReq;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller for managing questions in a survey.
 * Provides endpoints to:
 *  - add a question,
 *  - list all questions,
 *  - get a specific question,
 *  - update an existing question,
 *  - and delete a question.
 */

@RestController
@RequestMapping("/api/surveys/{surveyId}/questions")
public class QuestionController {

    private final QuestionService questions;

    public QuestionController(QuestionService questions) {
        this.questions = questions;
    }

    /**
     * Endpoint to list all questions for a specific survey, ordered by their position.
     *
     * @param surveyId The ID of the survey whose questions are to be listed.
     * @return A list of questions for the given survey, wrapped in QuestionRes DTOs.
     */
    @GetMapping
    public List<QuestionRes> list(@PathVariable UUID surveyId) {
        List<Question> qs = questions.list(surveyId);
        return qs.stream()
                .map(q -> QuestionRes.from(q, questions.options(q.getId())))
                .toList();
    }

    /**
     * Endpoint to create a new question for a given survey.
     *
     * @param surveyId The ID of the survey to which the question belongs.
     * @param req The request body containing question details such as text, type, and options.
     * @return The created question as a QuestionRes DTO.
     */
    @PostMapping
    public QuestionRes add(@PathVariable UUID surveyId, @Valid @RequestBody CreateQuestionReq req) {
        Question q = questions.create(surveyId, req);
        List<OptionChoice> opts = questions.options(q.getId());
        return QuestionRes.from(q, opts);
    }

    /**
     * Endpoint to update (replace) a specific question and its options.
     *
     * @param surveyId The ID of the survey containing the question.
     * @param questionId The ID of the question to update.
     * @param req The request body with the updated question details and options.
     * @return The updated question as a QuestionRes DTO.
     */
    @PutMapping("/{questionId}")
    public QuestionRes update(@PathVariable UUID surveyId,
                              @PathVariable UUID questionId,
                              @Valid @RequestBody UpdateQuestionReq req) {
        Question q = questions.update(questionId, req);
        return QuestionRes.from(q, questions.options(q.getId()));
    }

    /**
     * Endpoint to delete a specific question from a survey.
     *
     * @param surveyId The ID of the survey containing the question.
     * @param questionId The ID of the question to delete.
     */
    @DeleteMapping("/{questionId}")
    public void delete(@PathVariable UUID surveyId, @PathVariable UUID questionId) {
        questions.delete(surveyId, questionId);
    }

    /** REPLACE ALL: PUT /api/surveys/{surveyId}/questions  (bulk) */
    @PutMapping
    public List<QuestionRes> replaceAll(@PathVariable UUID surveyId,
                                        @Valid @RequestBody ReplaceQuestionsReq req) {
        List<Question> qs = questions.replaceAll(surveyId, req);
        return qs.stream()
                .map(q -> QuestionRes.from(q, questions.options(q.getId())))
                .toList();
    }

    /** DELETE ALL: DELETE /api/surveys/{surveyId}/questions */
    @DeleteMapping
    public void deleteAll(@PathVariable UUID surveyId) {
        questions.replaceAll(surveyId, new ReplaceQuestionsReq(List.of())); // replace with empty list
    }
}
