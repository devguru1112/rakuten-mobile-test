package com.rakuten.mobile.server.service;

import com.rakuten.mobile.server.domain.*;
import com.rakuten.mobile.server.repo.*;
import com.rakuten.mobile.server.tenancy.TenantContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service class to handle the submission and listing of responses to surveys.
 * It provides methods for validating and saving responses and answers to surveys.
 */
@Service
public class ResponseService {

    private final SurveyRepository sRepo;
    private final QuestionRepository qRepo;
    private final OptionChoiceRepository oRepo;
    private final ResponseRepository rRepo;
    private final AnswerRepository aRepo;

    public ResponseService(SurveyRepository sRepo, QuestionRepository qRepo, OptionChoiceRepository oRepo,
                           ResponseRepository rRepo, AnswerRepository aRepo) {
        this.sRepo = sRepo;
        this.qRepo = qRepo;
        this.oRepo = oRepo;
        this.rRepo = rRepo;
        this.aRepo = aRepo;
    }

    /**
     * Lists all responses for a specific survey, with pagination support.
     *
     * @param surveyId The ID of the survey to list responses for.
     * @param pageable Pagination information.
     * @return A page of responses for the specified survey.
     */
    public Page<Response> list(UUID surveyId, Pageable pageable) {
        return rRepo.findBySurveyId(surveyId, pageable);
    }

    /**
     * Validates and processes the answers submitted by a respondent for a survey.
     *
     * - Ensures the survey is active before submission.
     * - Validates the answers against the survey schema (required questions).
     * - Saves the response and the answers to the database.
     *
     * @param surveyId The ID of the survey.
     * @param respondentId The ID of the respondent submitting the answers.
     * @param answers The list of answers provided by the respondent.
     * @return The ID of the saved response.
     * @throws IllegalArgumentException If the survey is not found or there are missing required answers.
     * @throws IllegalStateException If the survey is not active.
     */
    @Transactional
    public UUID submit(UUID surveyId, UUID respondentId, List<Answer> answers) {
        // Ensure the survey exists and is active
        Survey s = sRepo.findById(surveyId).orElseThrow(() -> new IllegalArgumentException("Survey not found"));
        if (!"ACTIVE".equalsIgnoreCase(s.getStatus())) {
            throw new IllegalStateException("Survey not ACTIVE");
        }

        // Load the survey schema (questions)
        List<Question> qs = qRepo.findBySurveyIdOrderByPositionAsc(surveyId);
        Map<UUID, Question> qById = new HashMap<>();
        for (Question q : qs) qById.put(q.getId(), q);

        // For choice questions, load valid options (used for validation)
        Map<UUID, List<OptionChoice>> options = new HashMap<>();
        for (Question q : qs) {
            if (q.getType() == QuestionType.SINGLE_CHOICE || q.getType() == QuestionType.MULTI_CHOICE) {
                options.put(q.getId(), oRepo.findByQuestionIdOrderByPositionAsc(q.getId()));
            }
        }

        // Basic validation: ensure required questions are answered
        Set<UUID> answered = new HashSet<>();
        for (Answer a : answers) {
            Question q = qById.get(a.getQuestionId());
            if (q == null) throw new IllegalArgumentException("Unknown question: " + a.getQuestionId());
            answered.add(q.getId());
        }

        // Check if all required questions have been answered
        for (Question q : qs) {
            if (q.isRequired() && !answered.contains(q.getId())) {
                throw new IllegalArgumentException("Missing required answer for question: " + q.getId());
            }
        }

        // Persist the response and the answers
        UUID tenant = UUID.fromString(TenantContext.required());
        Response r = new Response();
        r.setTenantId(tenant);
        r.setSurveyId(surveyId);
        r.setRespondentId(respondentId);
        rRepo.save(r);

        // Save each answer, associating it with the response
        for (Answer a : answers) {
            a.setTenantId(tenant);
            a.setResponseId(r.getId());
            aRepo.save(a);
        }
        return r.getId(); 
    }
}
