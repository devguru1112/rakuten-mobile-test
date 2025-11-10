package com.rakuten.mobile.server.service;

import com.rakuten.mobile.server.domain.Survey;
import com.rakuten.mobile.server.events.SurveyPublishedEvent;
import com.rakuten.mobile.server.repo.SurveyRepository;
import com.rakuten.mobile.server.tenancy.TenantContext;
import com.rakuten.mobile.server.web.dto.CreateSurveyReq;
import com.rakuten.mobile.server.web.dto.SurveyUpdateRequest;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher events;

    public SurveyService(SurveyRepository repo, ApplicationEventPublisher events) {
        this.repo = repo;
        this.events = events;
    }

    /**
     * Creates a new survey for the current tenant.
     * The survey is initially set to a "DRAFT" status and a creation timestamp is set.
     *
     * @param req The CreateSurveyReq of the survey.
     * @return The created survey entity.
     */
    @Transactional
    public Survey create(CreateSurveyReq req) {
        Survey s = new Survey();
        s.setId(UUID.randomUUID());
        s.setTenantId(UUID.fromString(TenantContext.required()));
        if (req.title() != null && !req.title().isBlank()) s.setTitle(req.title());
        if (req.description() != null) s.setDescription(req.description());
        if (req.status() != null) s.setStatus(req.status()); else s.setStatus("DRAFT");
        s.setCreatedAt(Instant.now());
        s.setUpdatedAt(Instant.now());
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
     * Marks a survey as "ACTIVE" to so it can accept responses and publish an async event.
     * The status of the survey is updated, and the change is persisted automatically through dirty checking.
     *
     * @param id The ID of the survey to publish.
     * @return The updated survey entity with the status set to "ACTIVE".
     * @throws IllegalArgumentException If the survey is not found.
     */
    @Transactional
    public Survey publish(UUID id) {
        Survey s = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Survey not found"));
        s.setStatus("ACTIVE");   // dirty checking
        // Emit event outside the entity mutation so listeners can react asynchronously.
        events.publishEvent(new SurveyPublishedEvent(s.getTenantId(), s.getId(), s.getTitle()));
        return s;
    }


    @Transactional
    public Survey update(UUID surveyId, SurveyUpdateRequest req) {
        var s = repo.findById(surveyId)
                .orElseThrow(() -> new IllegalArgumentException("Survey not found"));

        if (req.title() != null && !req.title().isBlank()) s.setTitle(req.title());
        if (req.description() != null) s.setDescription(req.description());
        if (req.status() != null) s.setStatus(req.status());
        if (req.startsAt() != null) s.setStartsAt(req.startsAt());
        if (req.endsAt() != null) s.setEndsAt(req.endsAt());

        if (s.getStartsAt() != null && s.getEndsAt() != null && s.getEndsAt().isBefore(s.getStartsAt())) {
            throw new IllegalArgumentException("endsAt must be after startsAt");
        }

        return repo.save(s);
    }

    /**
     * Deletes a survey by its ID.
     *
     * @param id The ID of the survey to delete.
     */
    @Transactional
    public void delete(UUID id) { repo.deleteById(id); }


}
