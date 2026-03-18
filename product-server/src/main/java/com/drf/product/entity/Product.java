package com.drf.product.entity;

import com.drf.common.entity.BaseTimeEntity;
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

    @Column(nullable = false)
    private int price;

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

    public static Product create(Category category, String name, int price, String description,
                                 Integer discountRate, LocalDateTime saleStartAt, LocalDateTime saleEndAt) {
        return Product.builder()
                .category(category)
                .name(name)
                .price(price)
                .description(description)
                .status(ProductStatus.READY)
                .discountRate(Objects.requireNonNullElse(discountRate, 0))
                .saleStartAt(saleStartAt)
                .saleEndAt(saleEndAt)
                .build();
    }
}
