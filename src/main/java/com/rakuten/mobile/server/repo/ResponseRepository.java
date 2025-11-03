package com.rakuten.mobile.server.repo;

import com.rakuten.mobile.server.domain.Response;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Repository interface for accessing Response entities.
 * Provides methods for querying responses by their associated survey ID with pagination support.
 */
public interface ResponseRepository extends JpaRepository<Response, UUID> {
    // Finds all responses for a specific survey, with pagination support
    Page<Response> findBySurveyId(UUID surveyId, Pageable pageable);
}
