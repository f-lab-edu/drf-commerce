package com.drf.product.event.handler;

import com.drf.product.event.CreateProductEvent;
import com.drf.product.repository.ProductStockRedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductEventHandler {
    private final ProductStockRedisRepository productStockRedisRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCreateProductEvent(CreateProductEvent event) {
        CreateProductEvent.Payload payload = event.getPayload();
        try {
            productStockRedisRepository.setStock(payload.id(), payload.stock());
        } catch (Exception e) {
            log.error("Failed to sync stock to Redis, productId: {}, stock: {}",
                    payload.id(), payload.stock(), e);
        }
    }
}
