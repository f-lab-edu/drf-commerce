package com.drf.inventory.event.payload;

public record PaymentCompletedPayload(long productId, int quantity) {
}
