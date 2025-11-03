package com.rakuten.mobile.server.service;

import com.rakuten.mobile.server.domain.OptionChoice;
import com.rakuten.mobile.server.domain.Question;
import com.rakuten.mobile.server.domain.QuestionType;
import com.rakuten.mobile.server.repo.OptionChoiceRepository;
import com.rakuten.mobile.server.repo.QuestionRepository;
import com.rakuten.mobile.server.repo.SurveyRepository;
import com.rakuten.mobile.server.tenancy.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service class to manage questions and options for surveys.
 * Handles operations such as replacing all questions for a survey.
 */
@Service
public class QuestionService {

    private final QuestionRepository qRepo; // Repository for managing Question entities
    private final OptionChoiceRepository oRepo; // Repository for managing OptionChoice entities
    private final SurveyRepository sRepo; // Repository for managing Survey entities

    // Constructor to inject dependencies
    public QuestionService(QuestionRepository qRepo, OptionChoiceRepository oRepo, SurveyRepository sRepo) {
        this.qRepo = qRepo;
        this.oRepo = oRepo;
        this.sRepo = sRepo;
    }

    /**
     * Replaces all questions and their associated options for a survey.
     * - Deletes existing questions and options.
     * - Inserts new questions in the provided order.
     *
     * @param surveyId The ID of the survey whose questions are being replaced.
     * @param newQs The new list of questions to replace the old ones.
     * @param optionsByIndex A map where the key is the question index and the value is a list of options.
     * @return A list of saved questions.
     * @throws IllegalArgumentException If the survey is not found.
     */
    @Transactional
    public List<Question> replaceAll(UUID surveyId, List<Question> newQs, Map<Integer, List<OptionChoice>> optionsByIndex) {
        // Ensure the survey exists within the current tenant
        sRepo.findById(surveyId).orElseThrow(() -> new IllegalArgumentException("Survey not found"));

        // Delete all existing questions and options for the survey to ensure a clean slate
        qRepo.findBySurveyIdOrderByPositionAsc(surveyId).forEach(q -> {
            // Delete all options for each question
            oRepo.findByQuestionIdOrderByPositionAsc(q.getId()).forEach(o -> oRepo.deleteById(o.getId()));
            // Delete the question
            qRepo.deleteById(q.getId());
        });

        // Get the current tenant ID from TenantContext
        UUID tenant = UUID.fromString(TenantContext.required());

        List<Question> saved = new ArrayList<>();
        // Iterate over the new questions to save them
        for (int i = 0; i < newQs.size(); i++) {
            Question q = newQs.get(i);
            q.setId(UUID.randomUUID()); // Generate a new ID for the question
            q.setTenantId(tenant); // Set the tenant ID
            q.setSurveyId(surveyId); // Set the associated survey ID
            if (q.getPosition() == 0) q.setPosition(i + 1); // Set position if not provided
            if (q.getType() == null) q.setType(QuestionType.TEXT); // Default to TEXT type if not set

            // Save the question entity
            qRepo.save(q);
            saved.add(q);

            // Persist the options for the question, if any
            List<OptionChoice> opts = optionsByIndex.getOrDefault(i, List.of());
            int pos = 0;
            for (OptionChoice o : opts) {
                o.setId(UUID.randomUUID()); // Generate a new ID for the option
                o.setTenantId(tenant); // Set the tenant ID for the option
                o.setQuestionId(q.getId()); // Link the option to the question
                if (o.getPosition() == 0) o.setPosition(++pos); // Set the position if not provided
                oRepo.save(o); // Save the option entity
            }
        }
        return saved; // Return the list of saved questions
    }
}
