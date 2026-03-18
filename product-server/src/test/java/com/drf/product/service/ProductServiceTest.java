package com.drf.product.service;

import com.drf.common.exception.BusinessException;
import com.drf.product.common.exception.ErrorCode;
import com.drf.product.entity.Category;
import com.drf.product.entity.Product;
import com.drf.product.entity.ProductStatus;
import com.drf.product.entity.ProductStock;
import com.drf.product.event.CreateProductEvent;
import com.drf.product.model.request.ProductCreateRequest;
import com.drf.product.repository.CategoryRepository;
import com.drf.product.repository.ProductRepository;
import com.drf.product.repository.ProductStockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @InjectMocks
    private ProductService productService;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductStockRepository productStockRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;


    @Nested
    @DisplayName("상품 등록")
    class CreateProduct {
        private ProductCreateRequest request;
        private Category category;

        @BeforeEach
        void setUp() {
            request = ProductCreateRequest.builder()
                    .categoryId(1L)
                    .name("상품명")
                    .stock(100)
                    .price(10000)
                    .description("상품 설명")
                    .discountRate(10)
                    .saleStartAt(LocalDateTime.of(2026, 3, 1, 0, 0, 0))
                    .saleEndAt(LocalDateTime.of(2026, 4, 1, 0, 0, 0))
                    .build();

            category = Category.builder()
                    .name("카테고리")
                    .build();
        }

        @Test
        @DisplayName("등록 성공")
        void createProduct_success() {
            // given
            given(categoryRepository.findById(request.categoryId()))
                    .willReturn(Optional.of(category));

            Product savedProduct = Product.builder()
                    .id(1L)
                    .category(category)
                    .name(request.name())
                    .price(request.price())
                    .description(request.description())
                    .status(ProductStatus.READY)
                    .discountRate(request.discountRate())
                    .saleStartAt(request.saleStartAt())
                    .saleEndAt(request.saleEndAt())
                    .build();

            given(productRepository.save(any(Product.class)))
                    .willReturn(savedProduct);

            // when
            productService.createProduct(request);

            // then
            then(categoryRepository).should().findById(request.categoryId());
            then(productRepository).should().save(any(Product.class));
            then(productStockRepository).should().save(any(ProductStock.class));
            then(eventPublisher).should().publishEvent(any(CreateProductEvent.class));
        }

        @Test
        @DisplayName("세일 종료 시간이 시작 시간보다 과거일 경우 예외 발생")
        void createProduct_invalidSaleDateRange() {
            // given
            request = ProductCreateRequest.builder()
                    .categoryId(1L)
                    .name("상품명")
                    .stock(100)
                    .price(10000)
                    .description("상품 설명")
                    .discountRate(10)
                    .saleStartAt(LocalDateTime.of(2026, 3, 1, 0, 0, 0))
                    .saleEndAt(LocalDateTime.of(2000, 4, 1, 0, 0, 0))
                    .build();

            // when & then
            assertThatThrownBy(() -> productService.createProduct(request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.INVALID_SALE_DATE_RANGE);
        }

        @Test
        @DisplayName("존재하지 않는 카테고리로 등록 시 실패")
        void createProduct_categoryNotFound() {
            // given
            given(categoryRepository.findById(request.categoryId()))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> productService.createProduct(request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.CATEGORY_NOT_FOUND);

            then(productRepository).should(never()).save(any());
            then(productStockRepository).should(never()).save(any());
        }
    }
}
