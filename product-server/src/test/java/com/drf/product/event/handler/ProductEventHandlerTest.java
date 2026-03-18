package com.drf.product.event.handler;

import com.drf.product.event.CreateProductEvent;
import com.drf.product.repository.ProductStockRedisRepository;
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
        CreateProductEvent event = new CreateProductEvent(1L, 100);

        // when
        productEventHandler.handleCreateProductEvent(event);

        // then
        then(productStockRedisRepository).should().setStock(1L, 100);
    }
}
