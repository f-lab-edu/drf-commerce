package com.drf.order.model.response;

import com.drf.common.model.Money;
import com.drf.order.client.dto.response.InternalProductResponse;
import lombok.Builder;

import java.util.List;

@Builder
public record CheckoutAvailableItem(
        long productId,
        String name,
        long price,
        long discountedPrice,
        int quantity,
        long subtotal,
        List<Long> categoryPath
) {

    public static CheckoutAvailableItem of(InternalProductResponse product, int quantity, Money subtotal) {
        return CheckoutAvailableItem.builder()
                .productId(product.id())
                .name(product.name())
                .price(product.price())
                .discountedPrice(product.discountedPrice())
                .quantity(quantity)
                .subtotal(subtotal.toLong())
                .categoryPath(product.categoryPath())
                .build();
    }
}
