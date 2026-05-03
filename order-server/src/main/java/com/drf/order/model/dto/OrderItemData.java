package com.drf.order.model.dto;

import com.drf.common.model.Money;
import lombok.Builder;

@Builder
public record OrderItemData(
        long productId,
        String productName,
        Money unitPrice,
        Money discountedPrice,
        int quantity,
        Money productCouponDiscountAmount,
        Money orderCouponDiscountAmount,
        Money finalAmount,
        Long memberCouponId
) {
    public OrderItemData {
        if (productCouponDiscountAmount == null) productCouponDiscountAmount = Money.ZERO;
        if (orderCouponDiscountAmount == null) orderCouponDiscountAmount = Money.ZERO;
    }
}
