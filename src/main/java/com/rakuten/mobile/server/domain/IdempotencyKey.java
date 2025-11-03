package com.rakuten.mobile.server.domain;

import jakarta.persistence.*;
import lombok.Getter; import lombok.Setter;
import java.time.Instant;
import java.util.UUID;

@Getter @Setter
@Entity
@Table(name = "idempotency_keys")
@IdClass(IdempotencyKey.PK.class)
public class IdempotencyKey {

    @Id @Column(name = "tenant_id", columnDefinition = "uuid")
    private UUID tenantId;

    @Id @Column(name = "key", nullable = false)
    private String key;

    @Column(name = "response_id", nullable = false, columnDefinition = "uuid")
    private UUID responseId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    /** Composite PK holder */
    public static class PK implements java.io.Serializable {
        public UUID tenantId; public String key;
        public PK() {}
        public PK(UUID tenantId, String key) { this.tenantId = tenantId; this.key = key; }
    }
}