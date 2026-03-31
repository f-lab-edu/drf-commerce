package com.drf.coupon.model.response;

import com.drf.coupon.entity.ApplyType;
import com.drf.coupon.entity.Coupon;
import com.drf.coupon.entity.CouponStatus;
import com.drf.coupon.entity.DiscountType;

import java.time.LocalDateTime;

public record CouponListResponse(
        long couponId,
        String couponName,
        DiscountType discountType,
        int discountValue,
        Integer maxDiscountAmount,
        int minOrderAmount,
        ApplyType applyType,
        Long applyTargetId,
        LocalDateTime validFrom,
        LocalDateTime validUntil,
        int totalQuantity,
        int issuedQuantity,
        CouponStatus status
) {
    public static CouponListResponse from(Coupon coupon) {
        return new CouponListResponse(
                coupon.getId(),
                coupon.getName(),
                coupon.getDiscountType(),
                coupon.getDiscountValue(),
                coupon.getMaxDiscountAmount(),
                coupon.getMinOrderAmount(),
                coupon.getApplyType(),
                coupon.getApplyTargetId(),
                coupon.getValidFrom(),
                coupon.getValidUntil(),
                coupon.getTotalQuantity(),
                coupon.getIssuedQuantity(),
                coupon.getStatus()
        );
    }
}
