package com.drf.product.idempotency;

import com.drf.common.idempotency.IdempotencyLock;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RedisIdempotencyLock implements IdempotencyLock {

    private static final String LOCK_KEY_PREFIX = "idempotency:lock:";
    private static final Duration LOCK_TTL = Duration.ofSeconds(30);

    private final StringRedisTemplate redisTemplate;

    @Override
    public boolean acquire(String idempotencyKey, String scope) {
        String lockKey = generateKey(idempotencyKey, scope);
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", LOCK_TTL);
        return Boolean.TRUE.equals(acquired);
    }

    @Override
    public void release(String idempotencyKey, String scope) {
        redisTemplate.delete(generateKey(idempotencyKey, scope));
    }

    private String generateKey(String idempotencyKey, String scope) {
        return LOCK_KEY_PREFIX + idempotencyKey + ":" + scope;
    }
}
