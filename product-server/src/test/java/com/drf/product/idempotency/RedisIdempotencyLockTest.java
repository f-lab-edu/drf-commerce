package com.drf.product.idempotency;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class RedisIdempotencyLockTest {

    private static final String KEY = "550e8400-e29b-41d4-a716-446655440000";
    private static final String SCOPE = "STOCK_RESERVE";
    private static final String EXPECTED_LOCK_KEY = "idempotency:lock:" + KEY + ":" + SCOPE;
    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;
    @InjectMocks
    private RedisIdempotencyLock redisIdempotencyLock;

    @Test
    @DisplayName("락 선점 성공 - true 반환")
    void acquire_success() {
        // given
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.setIfAbsent(eq(EXPECTED_LOCK_KEY), eq("1"), eq(Duration.ofSeconds(30))))
                .willReturn(true);

        // when
        boolean result = redisIdempotencyLock.acquire(KEY, SCOPE);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("락 이미 존재 - false 반환")
    void acquire_alreadyLocked() {
        // given
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.setIfAbsent(eq(EXPECTED_LOCK_KEY), eq("1"), eq(Duration.ofSeconds(30))))
                .willReturn(false);

        // when
        boolean result = redisIdempotencyLock.acquire(KEY, SCOPE);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("락 해제 - Redis 키 삭제")
    void release_deletesKey() {
        // when
        redisIdempotencyLock.release(KEY, SCOPE);

        // then
        then(redisTemplate).should().delete(EXPECTED_LOCK_KEY);
    }
}
