package com.drf.order.client.dto.request;

import com.drf.order.model.request.ProductCouponAvailableRequest;
import lombok.Builder;

import java.util.List;

@Builder
public record InternalProductCouponRequest(
        long memberId,
        long cartItemId,
        long productId,
        long lineAmount,
        int quantity,
        List<Long> categoryPath,
        List<Long> usedMemberCouponIds
) {
    public static InternalProductCouponRequest from(
            long memberId,
            ProductCouponAvailableRequest request,
            List<Long> usedMemberCouponIds) {
        return InternalProductCouponRequest.builder()
                .memberId(memberId)
                .cartItemId(request.cartItemId())
                .productId(request.productId())
                .lineAmount(request.lineAmount())
                .quantity(request.quantity())
                .categoryPath(request.categoryPath())
                .usedMemberCouponIds(usedMemberCouponIds)
                .build();
    }
}
