package com.rakuten.mobile.server.events;

import java.util.UUID;

/** Domain event emitted when a survey transitions to ACTIVE. */
public record SurveyPublishedEvent(UUID tenantId, UUID surveyId, String title) {}
