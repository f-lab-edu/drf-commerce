package com.drf.product.event;

import com.drf.common.event.BaseEvent;

public class ProductUpdatedEvent extends BaseEvent<ProductUpdatedEvent.Payload> {

    public ProductUpdatedEvent(long id, int stock) {
        super(ProductEventType.UPDATE_PRODUCT.name(), new Payload(id, stock));
    }

    public record Payload(long id, int stock) {
    }
}
