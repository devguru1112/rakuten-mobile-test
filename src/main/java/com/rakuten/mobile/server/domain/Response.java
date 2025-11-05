package com.rakuten.mobile.server.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents a single submission of answers for a survey.
 * Each response is tied to a survey, a tenant, and optionally to a specific respondent.
 */
@Getter @Setter
@Entity
@Table(name = "responses")
//@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = String.class))
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public class Response {
    @Id @Column(columnDefinition = "uuid")
    private UUID id = UUID.randomUUID(); // Unique identifier for the response

    @Column(name = "tenant_id", nullable = false, columnDefinition = "uuid")
    private UUID tenantId; // Tenant ID for multi-tenancy

    @Column(name = "survey_id", nullable = false, columnDefinition = "uuid")
    private UUID surveyId; // Survey ID this response is for

    @Column(name = "respondent_id", columnDefinition = "uuid")
    private UUID respondentId; // Respondent ID (nullable for anonymous submissions)

    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt = Instant.now(); // Timestamp of when the response was submitted
}
