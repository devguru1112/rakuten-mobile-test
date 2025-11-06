package com.rakuten.mobile.server.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter @Setter
@Entity
@Table(
        name = "idempotency_keys",
        uniqueConstraints = @UniqueConstraint(name = "uq_idem_tenant_key", columnNames = {"tenant_id", "idem_key"})
)
public class IdempotencyKey {
    @Id
    @Column(columnDefinition = "uuid")
    private UUID id = UUID.randomUUID();            // <-- single surrogate PK

    @Column(name = "tenant_id", nullable = false, columnDefinition = "uuid")
    private UUID tenantId;

    @Column(name = "idem_key", nullable = false)    // <-- NOT "key"
    private String idemKey;

    @Column(name = "created_at_epoch", nullable = false)
    private long createdAtEpoch;

    @Column(name = "response_id", columnDefinition = "uuid")
    private UUID responseId;
}