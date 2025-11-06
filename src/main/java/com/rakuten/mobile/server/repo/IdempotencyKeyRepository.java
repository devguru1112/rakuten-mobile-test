package com.rakuten.mobile.server.repo;

import com.rakuten.mobile.server.domain.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, UUID> {
    Optional<IdempotencyKey> findByTenantIdAndIdemKey(UUID tenantId, String idemKey);
}