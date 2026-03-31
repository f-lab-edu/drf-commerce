package com.drf.coupon.model.response;

import com.drf.coupon.entity.ApplyType;
import com.drf.coupon.entity.DiscountType;
import com.drf.coupon.entity.MemberCoupon;
import com.drf.coupon.entity.MemberCouponStatus;

import java.time.LocalDateTime;

public record MemberCouponListResponse(
        long memberCouponId,
        String couponName,
        DiscountType discountType,
        int discountValue,
        Integer maxDiscountAmount,
        int minOrderAmount,
        ApplyType applyType,
        Long applyTargetId,
        LocalDateTime validFrom,
        LocalDateTime validUntil,
        MemberCouponStatus status
) {
    public static MemberCouponListResponse from(MemberCoupon memberCoupon) {
        return new MemberCouponListResponse(
                memberCoupon.getId(),
                memberCoupon.getCoupon().getName(),
                memberCoupon.getCoupon().getDiscountType(),
                memberCoupon.getCoupon().getDiscountValue(),
                memberCoupon.getCoupon().getMaxDiscountAmount(),
                memberCoupon.getCoupon().getMinOrderAmount(),
                memberCoupon.getCoupon().getApplyType(),
                memberCoupon.getCoupon().getApplyTargetId(),
                memberCoupon.getCoupon().getValidFrom(),
                memberCoupon.getCoupon().getValidUntil(),
                memberCoupon.getStatus()
        );
    }
}
