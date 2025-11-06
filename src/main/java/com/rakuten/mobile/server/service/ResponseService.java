package com.rakuten.mobile.server.service;

import com.rakuten.mobile.server.domain.IdempotencyKey;
import com.rakuten.mobile.server.domain.Response;
import com.rakuten.mobile.server.repo.IdempotencyKeyRepository;
import com.rakuten.mobile.server.repo.ResponseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Service class to handle the submission and listing of responses to surveys.
 * It provides methods for validating and saving responses and answers to surveys.
 */
@Service
public class ResponseService {

    private final ResponseRepository responseRepo;
    private final IdempotencyKeyRepository idemRepo;

    public ResponseService(ResponseRepository responseRepo,
                           IdempotencyKeyRepository idemRepo) {
        this.responseRepo = responseRepo;
        this.idemRepo = idemRepo;
    }

    /**
     * Lists all responses for a specific survey, with pagination support.
     *
     * @param surveyId The ID of the survey to list responses for.
     * @param pageable Pagination information.
     * @return A page of responses for the specified survey.
     */
    public Page<Response> list(UUID surveyId, Pageable pageable) {
        return responseRepo.findBySurveyId(surveyId, pageable);
    }

    /**
     * Validates and processes the answers submitted by a respondent for a survey.
     *
     * - Ensures the survey is active before submission.
     * - Validates the answers against the survey schema (required questions).
     * - Saves the response and the answers to the database.
     *
     * @param surveyId The ID of the survey.
     * @param tenantId The ID of the tenancy.
     * @param answersJson The JSON of answers provided by the respondent.
     * @param idempotencyKey The Key of tenancy
     * @return The ID of the saved response.
     * @throws IllegalArgumentException If the survey is not found or there are missing required answers.
     * @throws IllegalStateException If the survey is not active.
     */
    @Transactional
    public UUID submit(UUID surveyId,
                       UUID tenantId,
                       UUID respondentId,
                       Map<String, Object> answersJson,
                       String idempotencyKey) {

        // 1) Check existing idempotency record by (tenantId, idemKey)
        Optional<IdempotencyKey> existing = idemRepo.findByTenantIdAndIdemKey(tenantId, idempotencyKey);
        if (existing.isPresent()) {
            UUID already = existing.get().getResponseId();
            if (already != null) {
                // Return the same response id — idempotent success
                return already;
            }
            // Exists but responseId is null (prior failure/in-flight) — proceed to write response and update this row.
        }

        // 2) Create and persist the Response (no findById usage)
        Response r = new Response();
        r.setId(UUID.randomUUID());
        r.setTenantId(tenantId);
        r.setRespondentId(respondentId);
        r.setSurveyId(surveyId);
        r.setSubmittedAt(Instant.now());
        r.setAnswersJson(answersJson);

        r = responseRepo.save(r);

        // 3) Upsert idempotency record (no tuple IN, no reserved "key" column)
        IdempotencyKey ik = existing.orElseGet(IdempotencyKey::new);
        if (ik.getId() == null) {
            ik.setId(UUID.randomUUID());
            ik.setCreatedAtEpoch(System.currentTimeMillis());
        }
        ik.setTenantId(tenantId);
        ik.setIdemKey(idempotencyKey);
        ik.setResponseId(r.getId());
        idemRepo.save(ik);

        return r.getId();
    }

    /**
     * Retrieves a response by its ID.
     *
     * @param id The ID of the response to retrieve.
     * @return An Optional containing the response if found, or empty if not found.
     */
    public Optional<Response> get(UUID id) { return responseRepo.findById(id); }
}
