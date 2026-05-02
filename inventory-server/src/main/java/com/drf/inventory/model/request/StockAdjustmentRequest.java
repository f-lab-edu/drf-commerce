package com.drf.inventory.model.request;

import jakarta.validation.constraints.NotNull;

public record StockAdjustmentRequest(
        @NotNull
        Long productId,
        @NotNull
        Long amount
) {
}
