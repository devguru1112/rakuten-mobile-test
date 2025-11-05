package com.rakuten.mobile.server.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SurveyUpdateRequest(
        @Size(max = 200) String title,
        @Size(max = 2000) String description,
        // e.g. "DRAFT", "ACTIVE", "CLOSED" — keep as String if your entity uses String
        String status,
        Instant startsAt,
        Instant endsAt,
        Instant createdAt,
        Instant updatedAt
) {}