package com.drf.product.entity;


import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Entity
@Builder
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "product_stock")
public class ProductStock {

    @Id
    private Long productId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false)
    private int stock;

    @LastModifiedDate
    private LocalDateTime updatedAt;


    public static ProductStock create(Product product, int stock) {
        return ProductStock.builder()
                .product(product)
                .stock(stock)
                .build();
    }

    public void updateStock(int stock) {
        this.stock = stock;
    }
}
