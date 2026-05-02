package com.drf.coupon.model.response.internal;

import java.util.List;

public record InternalCartCouponCalculateResponse(
        boolean applicable,
        long totalDiscountAmount,
        List<InternalCouponItemResult> items
) {
}
