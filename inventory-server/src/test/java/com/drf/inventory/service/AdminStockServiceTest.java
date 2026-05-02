package com.drf.inventory.service;

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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class AdminStockServiceTest {

    @InjectMocks
    private AdminStockService adminStockService;

    @Mock
    private ProductStockRepository productStockRepository;

    @Mock
    private ProductStockRedisRepository stockRedisRepository;

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
            then(productStockRepository).should().save(any(ProductStock.class));
            then(stockRedisRepository).should().setStock(productId, stock);
        }
    }
}
