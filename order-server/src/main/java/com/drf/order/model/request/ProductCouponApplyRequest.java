package com.drf.order.model.request;

import java.util.List;

public record ProductCouponApplyRequest(
        long cartItemId,
        long lineAmount,
        int quantity,
        List<Long> categoryPath
) {
}
