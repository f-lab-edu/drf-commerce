package com.drf.inventory.model.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record StockCreateRequest(
        @NotNull
        Long productId,

        @NotNull
        @Min(0)
        Long stock
) {
}
