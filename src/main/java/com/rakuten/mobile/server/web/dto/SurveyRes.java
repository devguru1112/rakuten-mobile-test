package com.rakuten.mobile.server.web.dto;

import com.rakuten.mobile.server.domain.Survey;

import java.time.Instant;
import java.util.UUID;

/**
 * Response projection for representing survey details in a list or detailed view.
 * It includes survey ID, title, status, and creation timestamp.
 */
public record SurveyRes(UUID id, String title, String description, String status, Instant startsAt, Instant endsAt, Instant createdAt, Instant updatedAt) {

    /**
     * Static method to map a Survey entity to a SurveyRes DTO.
     *
     * @param s The Survey entity.
     * @return A SurveyRes DTO with the relevant survey data.
     */
    public static SurveyRes from(Survey s) {
        return new SurveyRes(s.getId(), s.getTitle(), s.getDescription(), s.getStatus(), s.getStartsAt(), s.getEndsAt(), s.getCreatedAt(), s.getUpdatededAt());
    }
}
