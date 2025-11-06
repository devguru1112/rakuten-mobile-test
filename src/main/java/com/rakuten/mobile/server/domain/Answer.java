package com.rakuten.mobile.server.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

/**
 * Represents an answer in the system, associated with a specific response and question.
 * The answer value is stored as a JSON object, allowing flexibility for different answer types.
 */
@Getter @Setter
@Entity
@Table(name = "answers")
//@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = String.class))
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")

public class Answer {
    @Id @Column(columnDefinition = "uuid")
    private UUID id = UUID.randomUUID(); // Unique identifier for the answer

    @Column(name = "tenant_id", nullable = false, columnDefinition = "uuid")
    private UUID tenantId; // Tenant ID for multi-tenancy support

    @Column(name = "response_id", nullable = false, columnDefinition = "uuid")
    private UUID responseId; // The response this answer belongs to

    @Column(name = "question_id", nullable = false, columnDefinition = "uuid")
    private UUID questionId; // The question this answer is related to

    @Column(name = "value_json", columnDefinition = "jsonb", nullable = false)
    @org.hibernate.annotations.JdbcTypeCode(SqlTypes.JSON)
    private Object valueJson; // Stores the answer value as a JSON object
}
