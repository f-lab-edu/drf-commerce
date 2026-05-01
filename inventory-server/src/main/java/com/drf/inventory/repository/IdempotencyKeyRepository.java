package com.drf.inventory.repository;

import com.drf.inventory.entity.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, Long> {
    Optional<IdempotencyKey> findByIdempotencyKeyAndScope(String idempotencyKey, String scope);
}
