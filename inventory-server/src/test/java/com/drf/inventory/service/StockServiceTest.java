package com.drf.inventory.service;

import com.drf.common.exception.BusinessException;
import com.drf.inventory.common.exception.ErrorCode;
import com.drf.inventory.model.request.StockBatchReleaseRequest;
import com.drf.inventory.model.request.StockBatchReserveRequest;
import com.drf.inventory.repository.ProductStockRedisRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @InjectMocks
    private StockService stockService;

    @Mock
    private ProductStockRedisRepository stockRedisRepository;

    @Nested
    @DisplayName("가용 재고 조회")
    class GetStocks {

        @Test
        @DisplayName("전체 조회 성공 - 요청한 모든 상품의 가용 재고를 반환한다")
        void success() {
            // given
            List<Long> productIds = List.of(1L, 2L);
            given(stockRedisRepository.getStocks(productIds)).willReturn(List.of(100L, 200L));

            // when
            var result = stockService.getStocks(productIds);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).productId()).isEqualTo(1L);
            assertThat(result.get(0).stock()).isEqualTo(100L);
            assertThat(result.get(1).productId()).isEqualTo(2L);
            assertThat(result.get(1).stock()).isEqualTo(200L);
        }

        @Test
        @DisplayName("Redis 키가 없는 상품은 결과에서 제외된다")
        void success_partialResult() {
            // given
            List<Long> productIds = List.of(1L, 999L);
            given(stockRedisRepository.getStocks(productIds)).willReturn(Arrays.asList(100L, null));

            // when
            var result = stockService.getStocks(productIds);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).productId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("전체 상품의 Redis 키가 없으면 빈 목록을 반환한다")
        void success_allNull() {
            // given
            List<Long> productIds = List.of(1L, 2L);
            given(stockRedisRepository.getStocks(productIds)).willReturn(Arrays.asList(null, null));

            // when
            var result = stockService.getStocks(productIds);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("빈 목록으로 요청하면 빈 목록을 반환한다")
        void success_emptyRequest() {
            // given
            given(stockRedisRepository.getStocks(List.of())).willReturn(List.of());

            // when
            var result = stockService.getStocks(List.of());

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("재고 선점")
    class ReserveProductStock {

        @Test
        @DisplayName("재고 선점 성공 - 남은 재고를 반환한다")
        void success() {
            // given
            long productId = 1L;
            var item = new StockBatchReserveRequest.StockBatchReserveItem(productId, 10L);
            StockBatchReserveRequest request = new StockBatchReserveRequest(List.of(item));

            given(stockRedisRepository.reserveStock(productId, 10)).willReturn(90);

            // when
            stockService.batchReserveStock(request);
        }

        @Test
        @DisplayName("Redis에 재고 키가 없으면 STOCK_NOT_FOUND 예외 발생")
        void fail_stockKeyNotFoundInRedis() {
            // given
            long productId = 1L;
            var item = new StockBatchReserveRequest.StockBatchReserveItem(productId, 10L);
            StockBatchReserveRequest request = new StockBatchReserveRequest(List.of(item));

            given(stockRedisRepository.reserveStock(productId, 10)).willReturn(-1);

            // when & then
            assertThatThrownBy(() -> stockService.batchReserveStock(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AVAILABLE_STOCK_NOT_FOUND);
        }

        @Test
        @DisplayName("재고가 부족하면 INSUFFICIENT_STOCK 예외 발생")
        void fail_insufficientStock() {
            // given
            long productId = 1L;
            var item = new StockBatchReserveRequest.StockBatchReserveItem(productId, 10L);
            StockBatchReserveRequest request = new StockBatchReserveRequest(List.of(item));

            given(stockRedisRepository.reserveStock(productId, 10)).willReturn(-2);

            // when & then
            assertThatThrownBy(() -> stockService.batchReserveStock(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INSUFFICIENT_AVAILABLE_STOCK);
        }
    }

    @Nested
    @DisplayName("재고 선점 해제")
    class ReleaseProductStock {

        @Test
        @DisplayName("재고 해제 성공 - 복원된 재고를 반환한다")
        void success() {
            // given
            long productId = 1L;
            var item = new StockBatchReleaseRequest.StockBatchReleaseItem(productId, 10L);
            StockBatchReleaseRequest request = new StockBatchReleaseRequest(List.of(item));

            given(stockRedisRepository.releaseStock(productId, 10)).willReturn(100);

            // when
            stockService.batchReleaseStock(request);
        }

        @Test
        @DisplayName("Redis에 재고 키가 없어도 best-effort라 예외가 전파되지 않는다")
        void fail_stockKeyNotFoundInRedis() {
            // given
            long productId = 1L;
            var item = new StockBatchReleaseRequest.StockBatchReleaseItem(productId, 10L);
            StockBatchReleaseRequest request = new StockBatchReleaseRequest(List.of(item));

            given(stockRedisRepository.releaseStock(productId, 10)).willReturn(-1);

            // when & then
            assertThatCode(() -> stockService.batchReleaseStock(request))
                    .doesNotThrowAnyException();
        }
    }
}
