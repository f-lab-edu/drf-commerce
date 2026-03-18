package com.drf.product.event;

import com.drf.common.event.BaseEvent;

public class ProductCreatedEvent extends BaseEvent<ProductCreatedEvent.Payload> {

    public ProductCreatedEvent(long id, int stock) {
        super(ProductEventType.CREATE_PRODUCT.name(), new Payload(id, stock));
    }

    public record Payload(long id, int stock) {
    }
}
