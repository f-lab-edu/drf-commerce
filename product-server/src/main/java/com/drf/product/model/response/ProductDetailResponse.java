package com.drf.product.model.response;

import com.drf.product.entity.Product;
import com.drf.product.entity.ProductStatus;
import com.drf.product.entity.ProductStock;

import java.time.LocalDateTime;

public record ProductDetailResponse(
        long id,
        String categoryName,
        String name,
        int price,
        String description,
        ProductStatus status,
        int discountRate,
        int stock,
        LocalDateTime saleStartAt,
        LocalDateTime saleEndAt
) {
    public static ProductDetailResponse from(Product product, ProductStock productStock) {
        return new ProductDetailResponse(
                product.getId(),
                product.getCategory().getName(),
                product.getName(),
                product.getPrice(),
                product.getDescription(),
                product.getStatus(),
                product.getDiscountRate(),
                productStock.getStock(),
                product.getSaleStartAt(),
                product.getSaleEndAt()
        );
    }
}
