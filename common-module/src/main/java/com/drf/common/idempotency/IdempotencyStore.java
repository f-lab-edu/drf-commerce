package com.drf.common.idempotency;

import java.util.Optional;

public interface IdempotencyStore {
    Optional<CachedResponse> findCachedResponse(String idempotencyKey, String scope);

    void saveResponse(String idempotencyKey, String scope, int statusCode, String response);
}
