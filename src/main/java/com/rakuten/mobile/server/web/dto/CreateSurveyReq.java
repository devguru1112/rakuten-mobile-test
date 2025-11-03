package com.rakuten.mobile.server.web.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for creating a new survey.
 * Contains the survey title as the only required field.
 */
public record CreateSurveyReq(@NotBlank String title) {}
