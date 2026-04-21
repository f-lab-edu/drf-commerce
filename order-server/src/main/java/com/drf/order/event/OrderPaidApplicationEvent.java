package com.drf.order.event;

import java.util.List;

public record OrderPaidApplicationEvent(
        long orderId,
        long memberId,
        List<Long> cartItemIds,
        List<Long> usedMemberCouponIds
) {
}
