package com.drf.inventory.service;

import com.drf.inventory.entity.ProductStock;
import com.drf.inventory.model.request.StockCreateRequest;
import com.drf.inventory.repository.ProductStockRedisRepository;
import com.drf.inventory.repository.ProductStockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminStockService {
    private final ProductStockRepository productStockRepository;
    private final ProductStockRedisRepository stockRedisRepository;

    @Transactional
    public void createStock(StockCreateRequest request) {
        ProductStock productStock = ProductStock.create(request.productId(), request.stock());
        productStockRepository.save(productStock);
        stockRedisRepository.setStock(request.productId(), request.stock());
    }
}
