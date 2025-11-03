package com.rakuten.mobile.server.repo;

import com.rakuten.mobile.server.domain.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Repository interface for accessing Question entities.
 * Provides methods for querying questions by their associated survey ID and ordering them by position.
 */
public interface QuestionRepository extends JpaRepository<Question, UUID> {
    // Finds all questions for a specific survey, ordered by position
    List<Question> findBySurveyIdOrderByPositionAsc(UUID surveyId);
}
