package com.drf.coupon.validation;

import com.drf.coupon.entity.Coupon;
import lombok.Builder;

@Builder
public record CouponValidationContext(
        Coupon coupon,
        Long memberId,
        Integer orderAmount,
        Integer categoryAmount
) {
    public static CouponValidationContext forIssue(Coupon coupon, Long memberId) {
        return CouponValidationContext.builder()
                .coupon(coupon)
                .memberId(memberId)
                .build();
    }

    public static CouponValidationContext forCalculate(Coupon coupon, int orderAmount, Integer categoryAmount) {
        return CouponValidationContext.builder()
                .coupon(coupon)
                .orderAmount(orderAmount)
                .categoryAmount(categoryAmount)
                .build();
    }
}
