package com.rakuten.mobile.server.service;

import com.rakuten.mobile.server.domain.Survey;
import com.rakuten.mobile.server.repo.SurveyRepository;
import com.rakuten.mobile.server.tenancy.TenantContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Service class to handle the business logic of the survey lifecycle.
 * Provides methods for creating, listing, retrieving, publishing, and deleting surveys.
 */
@Service
public class SurveyService {
    private final SurveyRepository repo;

    public SurveyService(SurveyRepository repo) { this.repo = repo; }

    /**
     * Creates a new survey for the current tenant.
     * The survey is initially set to a "DRAFT" status and a creation timestamp is set.
     *
     * @param title The title of the survey.
     * @return The created survey entity.
     */
    @Transactional
    public Survey create(String title) {
        Survey s = new Survey();
        s.setId(UUID.randomUUID());
        s.setTenantId(UUID.fromString(TenantContext.required()));
        s.setTitle(title);
        s.setStatus("DRAFT");
        s.setCreatedAt(Instant.now());
        return repo.save(s);
    }

    /**
     * Retrieves a paginated list of surveys filtered by status.
     * If the status is null or empty, all surveys are returned.
     *
     * @param status The status of the surveys to filter by (e.g., "ACTIVE", "DRAFT").
     * @param pageable Pagination information.
     * @return A page of surveys.
     */
    public Page<Survey> list(String status, Pageable pageable) {
        return (status == null || status.isBlank())
                ? repo.findAll(pageable)
                : repo.findByStatus(status, pageable);
    }

    /**
     * Retrieves a survey by its ID.
     *
     * @param id The ID of the survey to retrieve.
     * @return An Optional containing the survey if found, or empty if not found.
     */
    public Optional<Survey> get(UUID id) { return repo.findById(id); }

    /**
     * Marks a survey as "ACTIVE" to allow responses to be submitted.
     * The status of the survey is updated, and the change is persisted automatically through dirty checking.
     *
     * @param id The ID of the survey to publish.
     * @return The updated survey entity with the status set to "ACTIVE".
     * @throws IllegalArgumentException If the survey is not found.
     */
    @Transactional
    public Survey publish(UUID id) {
        Survey s = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Survey not found"));
        s.setStatus("ACTIVE");   // dirty checking will persist on tx commit
        return s;
    }

    /**
     * Deletes a survey by its ID.
     *
     * @param id The ID of the survey to delete.
     */
    @Transactional
    public void delete(UUID id) { repo.deleteById(id); }
}
