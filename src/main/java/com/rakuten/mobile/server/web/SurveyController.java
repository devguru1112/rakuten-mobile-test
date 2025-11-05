package com.rakuten.mobile.server.web;

import com.rakuten.mobile.server.domain.Survey;
import com.rakuten.mobile.server.service.SurveyService;
import com.rakuten.mobile.server.web.dto.CreateSurveyReq;
import com.rakuten.mobile.server.web.dto.SurveyRes;
import com.rakuten.mobile.server.web.dto.SurveyUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for managing surveys.
 * Provides CRUD operations for surveys through the following endpoints:
 * - Create a new survey.
 * - List surveys with optional status filtering and pagination.
 * - Retrieve a specific survey by its ID.
 * - Publish a survey (set its status to "ACTIVE").
 * - Delete a survey by its ID.
 */
@RestController
@RequestMapping("/api/surveys")
public class SurveyController {

    private final SurveyService surveys;

    public SurveyController(SurveyService surveys) { this.surveys = surveys; }

    /**
     * Endpoint to create a new survey.
     *
     * @param req The request body containing the survey title.
     * @return The created survey as a SurveyRes DTO.
     */
    @PostMapping
    public SurveyRes create(@Valid @RequestBody CreateSurveyReq req) {
        return SurveyRes.from(surveys.create(req));
    }

    /**
     * Endpoint to list surveys with optional status filtering and pagination.
     *
     * @param status The status of the surveys to filter by (optional).
     * @param pageable The pagination information.
     * @return A page of surveys wrapped in SurveyRes DTOs.
     */
    @GetMapping
    public Page<SurveyRes> list(@RequestParam(required = false) String status, Pageable pageable) {
        return surveys.list(status, pageable).map(SurveyRes::from);
    }

    /**
     * Endpoint to retrieve a specific survey by its ID.
     *
     * @param id The ID of the survey to retrieve.
     * @return The requested survey as a SurveyRes DTO.
     * @throws IllegalArgumentException If the survey is not found.
     */
    @GetMapping("/{id}")
    public SurveyRes get(@PathVariable UUID id) {
        Survey s = surveys.get(id).orElseThrow(() -> new IllegalArgumentException("Survey not found"));
        return SurveyRes.from(s);
    }

    /**
     * Endpoint to publish a survey, changing its status to "ACTIVE".
     *
     * @param id The ID of the survey to publish.
     * @return The published survey as a SurveyRes DTO.
     */
    @PatchMapping("/{id}/publish")
    public SurveyRes publish(@PathVariable UUID id) {
        return SurveyRes.from(surveys.publish(id));
    }

    /**
     * Endpoint to update a survey.
     *
     * @param id The ID of the survey to update.
     * @param req The request body containing the survey title.
     * @return The created survey as a SurveyRes DTO.
     */
    @PutMapping("/{id}")
    public SurveyRes update(
            @PathVariable UUID id,
            @Valid @RequestBody SurveyUpdateRequest req
    ) {
        return SurveyRes.from(surveys.update(id, req));
    }

    /**
     * Endpoint to delete a survey by its ID.
     *
     * @param id The ID of the survey to delete.
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        surveys.delete(id);
    }

}
