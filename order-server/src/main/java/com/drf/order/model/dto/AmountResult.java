package com.drf.order.model.dto;

import com.drf.common.model.Money;
import lombok.Builder;

@Builder
public record AmountResult(
        Money totalAmount,
        Money productDiscountAmount,
        Money productCouponDiscountAmount,
        Money orderCouponDiscountAmount,
        Money deliveryFee,
        Money finalAmount) {

    public AmountResult {
        if (productDiscountAmount == null) productDiscountAmount = Money.ZERO;
        if (productCouponDiscountAmount == null) productCouponDiscountAmount = Money.ZERO;
        if (orderCouponDiscountAmount == null) orderCouponDiscountAmount = Money.ZERO;
    }
}
