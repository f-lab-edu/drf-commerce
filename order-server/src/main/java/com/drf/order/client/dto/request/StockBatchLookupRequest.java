package com.drf.order.client.dto.request;

import java.util.List;

public record StockBatchLookupRequest(
        List<Long> productIds
) {
}
