package com.drf.inventory.repository;

import com.drf.inventory.entity.ProductStock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductStockRepository extends JpaRepository<ProductStock, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ProductStock> findByProductId(Long productId);

    @Modifying
    @Query("UPDATE ProductStock ps SET ps.stock = ps.stock - :quantity WHERE ps.productId = :productId AND ps.stock >= :quantity")
    int decrementStock(@Param("productId") long productId, @Param("quantity") long quantity);

    @Modifying
    @Query("UPDATE ProductStock ps SET ps.stock = ps.stock + :quantity WHERE ps.productId = :productId")
    int incrementStock(@Param("productId") long productId, @Param("quantity") long quantity);
}
