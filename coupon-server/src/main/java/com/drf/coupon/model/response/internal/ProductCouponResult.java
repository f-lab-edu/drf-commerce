package com.drf.coupon.model.response.internal;

public record ProductCouponResult(
        long memberCouponId,
        String name,
        long discountAmount,
        boolean best,
        boolean usedOnOtherItem
) {
}
