package com.drf.common.idempotency;

public record CachedResponse(int statusCode, String body) {
}
