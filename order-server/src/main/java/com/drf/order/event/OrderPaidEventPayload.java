package com.drf.order.event;

import java.util.List;

public record OrderPaidEventPayload(
        long orderId,
        long memberId,
        List<Long> cartItemIds,
        List<Long> usedMemberCouponIds
) {
}
