package com.drf.coupon.model.request.internal;

import java.util.List;

public record InternalProductCouponRequest(
        long memberId,
        long cartItemId,
        long productId,
        long lineAmount,
        int quantity,
        List<Long> categoryPath,
        List<Long> usedMemberCouponIds
) {
}
