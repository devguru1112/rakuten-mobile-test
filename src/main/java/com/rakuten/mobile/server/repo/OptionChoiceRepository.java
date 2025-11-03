package com.rakuten.mobile.server.repo;

import com.rakuten.mobile.server.domain.OptionChoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Repository interface for accessing OptionChoice entities.
 * Provides methods for querying option choices by their associated question ID and ordering them by position.
 */
public interface OptionChoiceRepository extends JpaRepository<OptionChoice, UUID> {
    // Finds all options for a specific question, ordered by position
    List<OptionChoice> findByQuestionIdOrderByPositionAsc(UUID questionId);
}
