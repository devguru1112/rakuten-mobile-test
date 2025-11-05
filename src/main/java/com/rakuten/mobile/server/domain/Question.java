package com.rakuten.mobile.server.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.util.UUID;

/**
 * Represents a question in a survey, which can be of various types (e.g., single choice, multi-choice, text).
 * Contains information like the question text, type, and required status.
 */
@Getter @Setter
@Entity
@Table(name = "questions")
//@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = String.class))
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public class Question {
    @Id @Column(columnDefinition = "uuid")
    private UUID id = UUID.randomUUID(); // Unique identifier for the question

    @Column(name = "tenant_id", nullable = false, columnDefinition = "uuid")
    private UUID tenantId; // Tenant ID for multi-tenancy

    @Column(name = "survey_id", nullable = false, columnDefinition = "uuid")
    private UUID surveyId; // Survey ID the question belongs to

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionType type = QuestionType.TEXT; // Type of the question (e.g., single-choice, text)

    @Column(nullable = false)
    private String text; // The actual question text

    @Column(nullable = false)
    private boolean required = true; // Whether the question is mandatory

    @Column(nullable = false)
    private int position; // Position of the question in the survey
}
