package com.drf.inventory.service;

import com.drf.common.exception.BusinessException;
import com.drf.inventory.common.exception.ErrorCode;
import com.drf.inventory.entity.ProductStock;
import com.drf.inventory.model.request.StockCreateRequest;
import com.drf.inventory.repository.ProductStockRedisRepository;
import com.drf.inventory.repository.ProductStockRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class AdminStockServiceTest {

    @InjectMocks
    private AdminStockService adminStockService;

    @Mock
    private ProductStockRepository productStockRepository;

    @Mock
    private ProductStockRedisRepository stockRedisRepository;

    @Nested
    @DisplayName("확정 재고 조회")
    class GetStocks {

        @Test
        @DisplayName("전체 조회 성공 - 요청한 모든 상품의 재고를 반환한다")
        void success() {
            // given
            List<Long> productIds = List.of(1L, 2L);
            List<ProductStock> productStocks = List.of(
                    ProductStock.create(1L, 100L),
                    ProductStock.create(2L, 200L)
            );
            given(productStockRepository.findAllById(productIds)).willReturn(productStocks);

            // when
            var result = adminStockService.getStocks(productIds);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).productId()).isEqualTo(1L);
            assertThat(result.get(0).stock()).isEqualTo(100L);
            assertThat(result.get(1).productId()).isEqualTo(2L);
            assertThat(result.get(1).stock()).isEqualTo(200L);
        }

        @Test
        @DisplayName("일부 상품이 DB에 없으면 존재하는 것만 반환한다")
        void success_partialResult() {
            // given
            List<Long> productIds = List.of(1L, 999L);
            List<ProductStock> productStocks = List.of(ProductStock.create(1L, 100L));
            given(productStockRepository.findAllById(productIds)).willReturn(productStocks);

            // when
            var result = adminStockService.getStocks(productIds);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).productId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("빈 목록으로 요청하면 빈 목록을 반환한다")
        void success_emptyRequest() {
            // given
            given(productStockRepository.findAllById(List.of())).willReturn(List.of());

            // when
            var result = adminStockService.getStocks(List.of());

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("재고 초기 등록")
    class CreateStock {
        @Test
        @DisplayName("재고 초기 등록 성공 - DB와 Redis에 모두 저장된다")
        void createStock_success() {
            // given
            long productId = 1L;
            long stock = 100L;
            StockCreateRequest request = new StockCreateRequest(productId, stock);

            // when
            adminStockService.createStock(request);

            // then
            then(productStockRepository).should().save(any());
            then(stockRedisRepository).should().setStock(productId, stock);
        }
    }

    @Nested
    @DisplayName("확정 재고 덮어쓰기")
    class UpdateStock {

        @Test
        @DisplayName("현재보다 큰 값으로 덮어쓰면 증가 처리된다")
        void success_increase() {
            // given
            long productId = 1L;
            ProductStock productStock = ProductStock.create(productId, 50L);
            given(productStockRepository.findByProductId(productId)).willReturn(Optional.of(productStock));
            given(stockRedisRepository.releaseStock(productId, 30L)).willReturn(80);

            // when
            adminStockService.updateStock(productId, 80L);

            // then
            then(productStockRepository).should().incrementStock(productId, 30L);
            then(stockRedisRepository).should().releaseStock(productId, 30L);
        }

        @Test
        @DisplayName("현재보다 작은 값으로 덮어쓰면 감소 처리된다")
        void success_decrease() {
            // given
            long productId = 1L;
            ProductStock productStock = ProductStock.create(productId, 80L);
            given(productStockRepository.findByProductId(productId)).willReturn(Optional.of(productStock));
            given(productStockRepository.decrementStock(productId, 30L)).willReturn(1);
            given(stockRedisRepository.reserveStock(productId, 30L)).willReturn(20);

            // when
            adminStockService.updateStock(productId, 50L);

            // then
            then(productStockRepository).should().decrementStock(productId, 30L);
            then(stockRedisRepository).should().reserveStock(productId, 30L);
        }

        @Test
        @DisplayName("현재와 동일한 값이면 DB/Redis 변경 없이 성공한다")
        void success_noOp() {
            // given
            long productId = 1L;
            ProductStock productStock = ProductStock.create(productId, 100L);
            given(productStockRepository.findByProductId(productId)).willReturn(Optional.of(productStock));

            // when
            assertThatCode(() -> adminStockService.updateStock(productId, 100L))
                    .doesNotThrowAnyException();

            // then
            then(productStockRepository).should(never()).incrementStock(anyLong(), anyLong());
            then(productStockRepository).should(never()).decrementStock(anyLong(), anyLong());
            then(stockRedisRepository).should(never()).releaseStock(anyLong(), anyLong());
            then(stockRedisRepository).should(never()).reserveStock(anyLong(), anyLong());
        }

        @Test
        @DisplayName("재고 레코드가 없으면 CONFIRMED_STOCK_NOT_FOUND 예외 발생")
        void fail_confirmedStockNotFound() {
            // given
            long productId = 999L;
            given(productStockRepository.findByProductId(productId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> adminStockService.updateStock(productId, 100L))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CONFIRMED_STOCK_NOT_FOUND);
        }

        @Test
        @DisplayName("증가 시 Redis 키가 없으면 AVAILABLE_STOCK_NOT_FOUND 예외 발생")
        void fail_availableStockNotFound_increase() {
            // given
            long productId = 1L;
            ProductStock productStock = ProductStock.create(productId, 50L);
            given(productStockRepository.findByProductId(productId)).willReturn(Optional.of(productStock));
            given(stockRedisRepository.releaseStock(productId, 30L)).willReturn(-1);

            // when & then
            assertThatThrownBy(() -> adminStockService.updateStock(productId, 80L))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AVAILABLE_STOCK_NOT_FOUND);
        }

        @Test
        @DisplayName("감소 시 Redis 키가 없으면 AVAILABLE_STOCK_NOT_FOUND 예외 발생")
        void fail_availableStockNotFound_decrease() {
            // given
            long productId = 1L;
            ProductStock productStock = ProductStock.create(productId, 80L);
            given(productStockRepository.findByProductId(productId)).willReturn(Optional.of(productStock));
            given(productStockRepository.decrementStock(productId, 30L)).willReturn(1);
            given(stockRedisRepository.reserveStock(productId, 30L)).willReturn(-1);

            // when & then
            assertThatThrownBy(() -> adminStockService.updateStock(productId, 50L))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AVAILABLE_STOCK_NOT_FOUND);
        }

        @Test
        @DisplayName("감소 시 가용 재고 부족이면 INSUFFICIENT_AVAILABLE_STOCK 예외 발생")
        void fail_insufficientAvailableStock() {
            // given
            long productId = 1L;
            ProductStock productStock = ProductStock.create(productId, 80L);
            given(productStockRepository.findByProductId(productId)).willReturn(Optional.of(productStock));
            given(productStockRepository.decrementStock(productId, 50L)).willReturn(1);
            given(stockRedisRepository.reserveStock(productId, 50L)).willReturn(-2);

            // when & then
            assertThatThrownBy(() -> adminStockService.updateStock(productId, 30L))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INSUFFICIENT_AVAILABLE_STOCK);
        }
    }

    @Nested
    @DisplayName("확정 재고 델타 조정")
    class AdjustStock {

        @Test
        @DisplayName("양수 delta면 증가 처리된다")
        void success_increase() {
            // given
            long productId = 1L;
            ProductStock productStock = ProductStock.create(productId, 50L);
            given(productStockRepository.findByProductId(productId)).willReturn(Optional.of(productStock));
            given(stockRedisRepository.releaseStock(productId, 20L)).willReturn(70);

            // when
            adminStockService.adjustStock(productId, 20L);

            // then
            then(productStockRepository).should().incrementStock(productId, 20L);
            then(stockRedisRepository).should().releaseStock(productId, 20L);
        }

        @Test
        @DisplayName("음수 delta면 감소 처리된다")
        void success_decrease() {
            // given
            long productId = 1L;
            ProductStock productStock = ProductStock.create(productId, 50L);
            given(productStockRepository.findByProductId(productId)).willReturn(Optional.of(productStock));
            given(productStockRepository.decrementStock(productId, 20L)).willReturn(1);
            given(stockRedisRepository.reserveStock(productId, 20L)).willReturn(10);

            // when
            adminStockService.adjustStock(productId, -20L);

            // then
            then(productStockRepository).should().decrementStock(productId, 20L);
            then(stockRedisRepository).should().reserveStock(productId, 20L);
        }

        @Test
        @DisplayName("delta가 0이면 DB/Redis 변경 없이 성공한다")
        void success_noOp() {
            // given
            long productId = 1L;
            ProductStock productStock = ProductStock.create(productId, 50L);
            given(productStockRepository.findByProductId(productId)).willReturn(Optional.of(productStock));

            // when
            assertThatCode(() -> adminStockService.adjustStock(productId, 0L))
                    .doesNotThrowAnyException();

            // then
            then(productStockRepository).should(never()).incrementStock(anyLong(), anyLong());
            then(productStockRepository).should(never()).decrementStock(anyLong(), anyLong());
            then(stockRedisRepository).should(never()).releaseStock(anyLong(), anyLong());
            then(stockRedisRepository).should(never()).reserveStock(anyLong(), anyLong());
        }

        @Test
        @DisplayName("재고 레코드가 없으면 CONFIRMED_STOCK_NOT_FOUND 예외 발생")
        void fail_confirmedStockNotFound() {
            // given
            long productId = 999L;
            given(productStockRepository.findByProductId(productId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> adminStockService.adjustStock(productId, 10L))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CONFIRMED_STOCK_NOT_FOUND);
        }

        @Test
        @DisplayName("감소량이 확정 재고를 초과하면 NEGATIVE_STOCK_NOT_ALLOWED 예외 발생")
        void fail_negativeStockNotAllowed() {
            // given
            long productId = 1L;
            ProductStock productStock = ProductStock.create(productId, 10L);
            given(productStockRepository.findByProductId(productId)).willReturn(Optional.of(productStock));
            given(productStockRepository.decrementStock(productId, 50L)).willReturn(0);

            // when & then
            assertThatThrownBy(() -> adminStockService.adjustStock(productId, -50L))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NEGATIVE_STOCK_NOT_ALLOWED);
        }

        @Test
        @DisplayName("증가 시 Redis 키가 없으면 AVAILABLE_STOCK_NOT_FOUND 예외 발생")
        void fail_availableStockNotFound_increase() {
            // given
            long productId = 1L;
            ProductStock productStock = ProductStock.create(productId, 50L);
            given(productStockRepository.findByProductId(productId)).willReturn(Optional.of(productStock));
            given(stockRedisRepository.releaseStock(productId, 20L)).willReturn(-1);

            // when & then
            assertThatThrownBy(() -> adminStockService.adjustStock(productId, 20L))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AVAILABLE_STOCK_NOT_FOUND);
        }

        @Test
        @DisplayName("감소 시 Redis 키가 없으면 AVAILABLE_STOCK_NOT_FOUND 예외 발생")
        void fail_availableStockNotFound_decrease() {
            // given
            long productId = 1L;
            ProductStock productStock = ProductStock.create(productId, 50L);
            given(productStockRepository.findByProductId(productId)).willReturn(Optional.of(productStock));
            given(productStockRepository.decrementStock(productId, 20L)).willReturn(1);
            given(stockRedisRepository.reserveStock(productId, 20L)).willReturn(-1);

            // when & then
            assertThatThrownBy(() -> adminStockService.adjustStock(productId, -20L))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AVAILABLE_STOCK_NOT_FOUND);
        }

        @Test
        @DisplayName("감소 시 선점된 재고를 초과하면 INSUFFICIENT_AVAILABLE_STOCK 예외 발생")
        void fail_insufficientAvailableStock() {
            // given
            long productId = 1L;
            ProductStock productStock = ProductStock.create(productId, 50L);
            given(productStockRepository.findByProductId(productId)).willReturn(Optional.of(productStock));
            given(productStockRepository.decrementStock(productId, 40L)).willReturn(1);
            given(stockRedisRepository.reserveStock(productId, 40L)).willReturn(-2);

            // when & then
            assertThatThrownBy(() -> adminStockService.adjustStock(productId, -40L))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INSUFFICIENT_AVAILABLE_STOCK);
        }
    }
}
