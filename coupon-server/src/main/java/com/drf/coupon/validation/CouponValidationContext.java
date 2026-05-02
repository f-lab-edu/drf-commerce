package com.drf.coupon.validation;

import com.drf.common.model.Money;
import com.drf.coupon.entity.Coupon;
import lombok.Builder;

@Builder
public record CouponValidationContext(
        Coupon coupon,
        Long memberId,
        Money orderAmount,
        Money categoryAmount
) {
    public static CouponValidationContext forIssue(Coupon coupon, Long memberId) {
        return CouponValidationContext.builder()
                .coupon(coupon)
                .memberId(memberId)
                .build();
    }
}
