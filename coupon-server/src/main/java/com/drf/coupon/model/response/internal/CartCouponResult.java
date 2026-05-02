package com.drf.coupon.model.response.internal;

import com.drf.common.model.Money;
import lombok.Getter;

import java.util.List;

@Getter
public class CartCouponResult {

    private final long memberCouponId;
    private final String name;
    private final long discountAmount;
    private final List<InternalCouponItemResult> items;
    private boolean isBest;

    public CartCouponResult(long memberCouponId, String name, Money discountAmount, List<InternalCouponItemResult> items) {
        this.memberCouponId = memberCouponId;
        this.name = name;
        this.discountAmount = discountAmount.toLong();
        this.isBest = false;
        this.items = items;
    }

    public void markAsBest() {
        this.isBest = true;
    }
}
