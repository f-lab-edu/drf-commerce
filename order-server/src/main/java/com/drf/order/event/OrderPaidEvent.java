package com.drf.order.event;

import com.drf.common.event.BaseEvent;

public class OrderPaidEvent extends BaseEvent<OrderPaidEventPayload> {

    public OrderPaidEvent(OrderPaidEventPayload payload) {
        super("ORDER_PAID", payload);
    }
}
