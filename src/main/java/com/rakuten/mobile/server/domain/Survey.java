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
 * Represents the header of a survey, which contains the survey's title and status.
 * Only active surveys accept responses.
 */
@Getter @Setter
@Entity
@Table(name = "surveys")
//@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = String.class))
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public class Survey {
    @Id @Column(columnDefinition = "uuid")
    private UUID id = UUID.randomUUID(); // Unique identifier for the survey

    @Column(name = "tenant_id", nullable = false, columnDefinition = "uuid")
    private UUID tenantId; // Tenant ID for multi-tenancy

    @Column(nullable = false)
    private String title; // Title of the survey

    @Column(nullable = false)
    private String status = "DRAFT"; // Status of the survey (e.g., DRAFT, ACTIVE, ARCHIVED)

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now(); // Timestamp when the survey was created
}
