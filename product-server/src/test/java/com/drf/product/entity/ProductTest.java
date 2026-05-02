package com.drf.product.entity;

import com.drf.common.exception.BusinessException;
import com.drf.common.model.Money;
import com.drf.product.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ProductTest {

    private Category category;
    private LocalDateTime saleStartAt;
    private LocalDateTime saleEndAt;

    @BeforeEach
    void setUp() {
        category = Category.builder()
                .id(1L)
                .build();

        saleStartAt = LocalDate.of(2026, 1, 1).atStartOfDay();
        saleEndAt = LocalDate.of(2027, 1, 1).atStartOfDay();
    }

    private Product createProduct() {
        return Product.create(
                category,
                "상품",
                10000L,
                "설명",
                10,
                saleStartAt,
                saleEndAt
        );
    }

    // ===== Price =====

    @Test
    void updatePrice_shouldThrowException_whenPriceIsNegative() {
        Product product = createProduct();

        assertThatThrownBy(() ->
                product.updatePrice(Money.of(-1))
        )
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_PRICE);
    }

    // ===== Discount Rate =====

    @ParameterizedTest
    @ValueSource(ints = {-1, 101})
    void updateDiscountRate_shouldThrowException_whenRateIsOutOfRange(int discountRate) {
        Product product = createProduct();

        assertThatThrownBy(() ->
                product.updateDiscountRate(discountRate)
        )
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_DISCOUNT_RATE);
    }

    // ===== Sale Schedule =====

    @Test
    void updateSaleSchedule_shouldThrowException_whenStartOrEndDateIsNull() {
        Product product = createProduct();

        assertThatThrownBy(() ->
                product.updateSaleSchedule(LocalDateTime.now(), null)
        )
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INCOMPLETE_SALE_DATE);

        assertThatThrownBy(() ->
                product.updateSaleSchedule(null, LocalDateTime.now())
        )
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INCOMPLETE_SALE_DATE);
    }

    @Test
    void updateSaleSchedule_shouldThrowException_whenEndDateIsBeforeStartDate() {
        Product product = createProduct();

        assertThatThrownBy(() ->
                product.updateSaleSchedule(
                        LocalDate.of(2026, 1, 2).atStartOfDay(),
                        LocalDate.of(2025, 1, 1).atStartOfDay()
                )
        )
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_SALE_DATE_RANGE);
    }

    // ===== Creation =====

    @Test
    void shouldCreateProduct_withoutSaleSchedule() {
        Product.create(
                category,
                "상품",
                10000L,
                "설명",
                10,
                null,
                null
        );
    }
}