package com.drf.inventory.event.handler;

import com.drf.inventory.event.ProductCreatedEvent;
import com.drf.inventory.event.ProductUpdatedEvent;
import com.drf.inventory.repository.ProductStockRedisRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class ProductEventHandlerTest {

    @Mock
    private ProductStockRedisRepository productStockRedisRepository;

    @InjectMocks
    private ProductEventHandler productEventHandler;

    @Test
    @DisplayName("상품 생성 이벤트 수신 시 Redis에 재고를 저장한다")
    void handleCreateProductEvent() {
        // given
        ProductCreatedEvent event = new ProductCreatedEvent(1L, 100);

        // when
        productEventHandler.handleCreatedProductEvent(event);

        // then
        then(productStockRedisRepository).should().setStock(1L, 100);
    }

    @Test
    @DisplayName("상품 수정 이벤트 수신 시 Redis에 재고를 저장한다")
    void handleUpdateProductEvent() {
        // given
        ProductUpdatedEvent event = new ProductUpdatedEvent(1L, 100);

        // when
        productEventHandler.handleUpdatedProductEvent(event);

        // then
        then(productStockRedisRepository).should().setStock(1L, 100);
    }
}
