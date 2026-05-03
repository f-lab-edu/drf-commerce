package com.drf.coupon.model.request.internal;

import java.util.List;

public record InternalCartCouponItemRequest(
        long cartItemId,
        long productId,
        long lineAmount,
        int quantity,
        List<Long> categoryPath
) {
}
