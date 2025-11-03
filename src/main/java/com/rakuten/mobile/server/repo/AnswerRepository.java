package com.rakuten.mobile.server.repo;

import com.rakuten.mobile.server.domain.Answer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Repository interface for accessing Answer entities.
 * Provides methods for querying answers by their associated response ID.
 */
public interface AnswerRepository extends JpaRepository<Answer, UUID> {
    // Finds all answers associated with a specific response ID
    List<Answer> findByResponseId(UUID responseId);
}
