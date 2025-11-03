package com.rakuten.mobile.server.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import java.util.UUID;

/**
 * Represents a possible choice for a single or multi-choice question.
 * Each option is tied to a specific question and can have a display text and position.
 */
@Getter @Setter
@Entity
@Table(name = "options")
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = String.class))
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public class OptionChoice {
    @Id @Column(columnDefinition = "uuid")
    private UUID id = UUID.randomUUID(); // Unique identifier for the option

    @Column(name = "tenant_id", nullable = false, columnDefinition = "uuid")
    private UUID tenantId; // Tenant ID for multi-tenancy

    @Column(name = "question_id", nullable = false, columnDefinition = "uuid")
    private UUID questionId; // The question this option belongs to

    @Column(nullable = false)
    private String text; // Text for the option

    @Column(nullable = false)
    private int position; // Position of the option in the list
}
