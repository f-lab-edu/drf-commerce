package com.drf.product.service;

import com.drf.common.exception.BusinessException;
import com.drf.product.common.exception.ErrorCode;
import com.drf.product.entity.Category;
import com.drf.product.entity.Product;
import com.drf.product.entity.ProductStock;
import com.drf.product.event.CreateProductEvent;
import com.drf.product.model.request.ProductCreateRequest;
import com.drf.product.repository.CategoryRepository;
import com.drf.product.repository.ProductRepository;
import com.drf.product.repository.ProductStockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductStockRepository productStockRepository;
    private final ApplicationEventPublisher eventPublisher;


    @Transactional
    public Long createProduct(ProductCreateRequest request) {
        validateDateRange(request.saleStartAt(), request.saleEndAt());

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        Product product = Product.create(category, request.name(), request.price(), request.description(),
                request.discountRate(), request.saleStartAt(), request.saleEndAt());

        Product savedProduct = productRepository.save(product);

        ProductStock productStock = ProductStock.create(savedProduct, request.stock());
        productStockRepository.save(productStock);

        eventPublisher.publishEvent(new CreateProductEvent(savedProduct.getId(), request.stock()));

        return savedProduct.getId();
    }

    private void validateDateRange(LocalDateTime startAt, LocalDateTime endAt) {
        if (startAt != null && endAt != null && !endAt.isAfter(startAt)) {
            throw new BusinessException(ErrorCode.INVALID_SALE_DATE_RANGE);
        }
    }
}
