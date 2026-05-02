package com.drf.inventory.service;

import com.drf.common.exception.BusinessException;
import com.drf.inventory.common.exception.ErrorCode;
import com.drf.inventory.entity.ProductStock;
import com.drf.inventory.model.request.StockCreateRequest;
import com.drf.inventory.model.response.StockResponse;
import com.drf.inventory.repository.ProductStockRedisRepository;
import com.drf.inventory.repository.ProductStockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminStockService {
    private final ProductStockRepository productStockRepository;
    private final ProductStockRedisRepository stockRedisRepository;

    @Transactional(readOnly = true)
    public List<StockResponse> getStocks(List<Long> productIds) {
        return productStockRepository.findAllById(productIds).stream()
                .map(ps -> new StockResponse(ps.getProductId(), ps.getStock()))
                .toList();
    }

    @Transactional
    public void createStock(StockCreateRequest request) {
        ProductStock productStock = ProductStock.create(request.productId(), request.stock());
        productStockRepository.save(productStock);
        stockRedisRepository.setStock(request.productId(), request.stock());
    }

    @Transactional
    public void updateStock(Long productId, Long newTotal) {
        ProductStock productStock = productStockRepository.findByProductId(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CONFIRMED_STOCK_NOT_FOUND));

        applyDelta(productId, newTotal - productStock.getStock());
    }

    @Transactional
    public void adjustStock(Long productId, Long amount) {
        productStockRepository.findByProductId(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CONFIRMED_STOCK_NOT_FOUND));

        applyDelta(productId, amount);
    }

    private void applyDelta(Long productId, Long delta) {
        if (delta == 0) return;

        long absDelta = Math.abs(delta);
        boolean increase = delta > 0;

        applyToConfirmedStock(productId, absDelta, increase);
        applyToAvailableStock(productId, absDelta, increase);
    }

    private void applyToConfirmedStock(Long productId, long amount, boolean increase) {
        if (increase) {
            productStockRepository.incrementStock(productId, amount);
        } else {
            int affectedRows = productStockRepository.decrementStock(productId, amount);
            if (affectedRows == 0) {
                throw new BusinessException(ErrorCode.NEGATIVE_STOCK_NOT_ALLOWED);
            }
        }
    }

    private void applyToAvailableStock(Long productId, long amount, boolean increase) {
        int result = increase
                ? stockRedisRepository.releaseStock(productId, amount)
                : stockRedisRepository.reserveStock(productId, amount);

        switch (result) {
            case -1:
                throw new BusinessException(ErrorCode.AVAILABLE_STOCK_NOT_FOUND);
            case -2:
                throw new BusinessException(ErrorCode.INSUFFICIENT_AVAILABLE_STOCK);
        }
    }
}
