package com.drf.product.model.response;

import com.drf.product.entity.Product;
import com.drf.product.entity.ProductStatus;
import com.drf.product.entity.ProductStock;

import java.time.LocalDateTime;

public record ProductListResponse(
        long id,
        String categoryName,
        String name,
        int price,
        ProductStatus status,
        int discountRate,
        int stock,
        LocalDateTime createdAt
) {
    public static ProductListResponse from(Product product, ProductStock productStock) {
        return new ProductListResponse(
                product.getId(),
                product.getCategory().getName(),
                product.getName(),
                product.getPrice(),
                product.getStatus(),
                product.getDiscountRate(),
                productStock != null ? productStock.getStock() : 0,
                product.getCreatedAt()
        );
    }
}
