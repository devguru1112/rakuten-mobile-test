package com.rakuten.mobile.server.domain;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "idempotency_keys", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IdempotencyKey {

    @EmbeddedId
    private PK id;

    @Column(name = "response_id", nullable = false, columnDefinition = "uuid")
    private UUID responseId;

    @Column(name = "created_at_epoch", nullable = false)
    private Long createdAtEpoch;

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class PK implements Serializable {

        @Column(name = "tenant_id", nullable = false, columnDefinition = "uuid")
        private UUID tenantId;

        @Column(name = "key", nullable = false, length = 128)
        private String key;
    }
}
