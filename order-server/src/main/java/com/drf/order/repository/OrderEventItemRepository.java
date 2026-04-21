package com.drf.order.repository;

import com.drf.order.entity.OrderEventItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderEventItemRepository extends JpaRepository<OrderEventItem, Long> {
}
