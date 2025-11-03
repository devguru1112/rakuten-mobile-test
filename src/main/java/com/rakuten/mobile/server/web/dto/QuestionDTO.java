package com.rakuten.mobile.server.web.dto;

import com.rakuten.mobile.server.domain.QuestionType;

import java.util.List;
import java.util.UUID;

/**
 * Data Transfer Object for a question in the survey.
 * Includes optional options for choice-based questions (SINGLE_CHOICE, MULTI_CHOICE).
 */
public record QuestionDTO(
        UUID id,                // Unique identifier for the question
        QuestionType type,      // The type of the question (e.g., SINGLE_CHOICE, TEXT)
        String text,            // The text of the question
        boolean required,       // Whether the question is required to answer
        int position,           // Position of the question in the survey
        List<OptionDTO> options // Optional list of options for choice questions
) {
    public record OptionDTO(UUID id, String text, int position) {}
}

