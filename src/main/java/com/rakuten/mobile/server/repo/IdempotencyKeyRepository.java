package com.rakuten.mobile.server.repo;

import com.rakuten.mobile.server.domain.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, IdempotencyKey.PK> {
    @Query("""
           select i
           from IdempotencyKey i
           where i.id.tenantId = :tenantId and i.id.key = :key
           """)
    Optional<IdempotencyKey> findById_TenantIdAndIdKey(@Param("tenantId") UUID tenantId,
                                                  @Param("key") String key);
}
