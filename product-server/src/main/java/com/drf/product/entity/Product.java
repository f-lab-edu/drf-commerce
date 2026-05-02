package com.drf.product.entity;

import com.drf.common.converter.MoneyConverter;
import com.drf.common.entity.BaseTimeEntity;
import com.drf.common.exception.BusinessException;
import com.drf.common.model.Money;
import com.drf.product.common.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "product")
public class Product extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, length = 100)
    private String name;

    @Convert(converter = MoneyConverter.class)
    @Column(nullable = false)
    private Money price;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductStatus status;

    @Column(nullable = false)
    private int discountRate;

    private LocalDateTime saleStartAt;
    private LocalDateTime saleEndAt;

    private LocalDateTime deletedAt;

    public static Product create(Category category, String name, long price, String description,
                                 Integer discountRate, LocalDateTime saleStartAt, LocalDateTime saleEndAt) {
        validateDiscountRate(discountRate);
        validateSaleSchedule(saleStartAt, saleEndAt);

        return Product.builder()
                .category(category)
                .name(name)
                .price(Money.of(price))
                .description(description)
                .status(ProductStatus.READY)
                .discountRate(discountRate)
                .saleStartAt(saleStartAt)
                .saleEndAt(saleEndAt)
                .build();
    }

    private static void validateSaleSchedule(LocalDateTime startAt, LocalDateTime endAt) {
        if ((startAt == null) != (endAt == null)) {
            throw new BusinessException(ErrorCode.INCOMPLETE_SALE_DATE);
        }
        if (startAt != null && !endAt.isAfter(startAt)) {
            throw new BusinessException(ErrorCode.INVALID_SALE_DATE_RANGE);
        }
    }

    private static void validateDiscountRate(int discountRate) {
        if (discountRate < 0 || discountRate > 100) {
            throw new BusinessException(ErrorCode.INVALID_DISCOUNT_RATE);
        }
    }

    public void updateCategory(Category category) {
        Objects.requireNonNull(category);
        this.category = category;
    }

    public void updateName(String name) {
        Objects.requireNonNull(name);
        this.name = name;
    }

    public void updatePrice(Money price) {
        Objects.requireNonNull(price);
        if (price.isNegative()) {
            throw new BusinessException(ErrorCode.INVALID_PRICE);
        }
        this.price = price;
    }

    public void updateDescription(String description) {
        Objects.requireNonNull(description);
        this.description = description;
    }

    public void delete() {
        this.status = ProductStatus.DELETED;
        this.deletedAt = LocalDateTime.now();
    }

    public Money calculateDiscountAmount() {
        return this.price.calculateDiscountAmount(this.discountRate);
    }

    public Money calculateDiscountedPrice() {
        return this.price.subtract(calculateDiscountAmount());
    }

    public void updateDiscountRate(int discountRate) {
        validateDiscountRate(discountRate);
        this.discountRate = discountRate;
    }

    public void updateSaleSchedule(LocalDateTime startAt, LocalDateTime endAt) {
        validateSaleSchedule(startAt, endAt);
        this.saleStartAt = startAt;
        this.saleEndAt = endAt;
    }
}
