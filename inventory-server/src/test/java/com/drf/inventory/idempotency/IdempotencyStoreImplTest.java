package com.drf.inventory.idempotency;

import com.drf.common.idempotency.CachedResponse;
import com.drf.inventory.entity.IdempotencyKey;
import com.drf.inventory.repository.IdempotencyKeyRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class IdempotencyStoreImplTest {

    private static final String KEY = "550e8400-e29b-41d4-a716-446655440000";
    private static final String SCOPE = "STOCK_RESERVE";
    private static final int STATUS = 200;
    private static final String RESPONSE = "{\"code\":\"SUCCESS\"}";
    @Mock
    private IdempotencyKeyRepository idempotencyKeyRepository;
    @InjectMocks
    private IdempotencyStoreImpl idempotencyStoreImpl;

    @Test
    @DisplayName("캐시된 응답이 있으면 status와 body를 담은 CachedResponse를 반환한다")
    void findCachedResponse_found() {
        // given
        IdempotencyKey entity = IdempotencyKey.create(KEY, SCOPE, STATUS, RESPONSE);
        given(idempotencyKeyRepository.findByIdempotencyKeyAndScope(KEY, SCOPE))
                .willReturn(Optional.of(entity));

        // when
        Optional<CachedResponse> result = idempotencyStoreImpl.findCachedResponse(KEY, SCOPE);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().statusCode()).isEqualTo(STATUS);
        assertThat(result.get().body()).isEqualTo(RESPONSE);
    }

    @Test
    @DisplayName("캐시된 응답이 없으면 빈 Optional을 반환한다")
    void findCachedResponse_notFound() {
        // given
        given(idempotencyKeyRepository.findByIdempotencyKeyAndScope(KEY, SCOPE))
                .willReturn(Optional.empty());

        // when
        Optional<CachedResponse> result = idempotencyStoreImpl.findCachedResponse(KEY, SCOPE);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("status와 body를 함께 저장한다")
    void saveResponse_success() {
        // when
        idempotencyStoreImpl.saveResponse(KEY, SCOPE, STATUS, RESPONSE);

        // then
        then(idempotencyKeyRepository).should().save(any(IdempotencyKey.class));
    }

    @Test
    @DisplayName("중복 키 저장 시 DataIntegrityViolationException을 무시한다")
    void saveResponse_duplicateKey_ignored() {
        // given
        given(idempotencyKeyRepository.save(any())).willThrow(DataIntegrityViolationException.class);

        // when & then
        assertThatNoException().isThrownBy(() -> idempotencyStoreImpl.saveResponse(KEY, SCOPE, STATUS, RESPONSE));
    }
}
