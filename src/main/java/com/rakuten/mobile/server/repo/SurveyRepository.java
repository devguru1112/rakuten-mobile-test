package com.rakuten.mobile.server.repo;

import com.rakuten.mobile.server.domain.Survey;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Repository interface for accessing Survey entities.
 * Provides methods for querying surveys by their status with pagination support.
 */
public interface SurveyRepository extends JpaRepository<Survey, UUID> {
    // Finds surveys with a specific status, with pagination support
    Page<Survey> findByStatus(String status, Pageable pageable);
}
