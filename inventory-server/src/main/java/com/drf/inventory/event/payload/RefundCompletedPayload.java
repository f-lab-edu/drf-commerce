package com.drf.inventory.event.payload;

public record RefundCompletedPayload(long productId, int quantity) {
}
