package com.drf.inventory.model.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record StockOverwriteRequest(
        @NotNull
        Long productId,
        @PositiveOrZero
        Long totalStock
) {
}
