package com.drf.inventory.entity;


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
    @Column(name = "product_id")
    private Long productId;

    @Column(nullable = false)
    private int stock;

    @LastModifiedDate
    private LocalDateTime updatedAt;


    public static ProductStock create(Long productId, int stock) {
        return ProductStock.builder()
                .productId(productId)
                .stock(stock)
                .build();
    }

    public void updateStock(int stock) {
        this.stock = stock;
    }
}
