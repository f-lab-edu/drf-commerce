package com.drf.order.client.dto.response;

public record InternalStockResponse(
        Long productId,
        long stock
) {
}
