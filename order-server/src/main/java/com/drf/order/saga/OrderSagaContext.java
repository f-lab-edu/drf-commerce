package com.drf.order.saga;

import com.drf.order.client.dto.response.DeliveryAddressResponse;
import com.drf.order.entity.OrderStatus;
import com.drf.order.model.dto.AmountResult;
import com.drf.order.model.dto.CartItemsResult;
import com.drf.order.model.dto.OrderLineItem;
import com.drf.order.model.request.OrderCreateRequest;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class OrderSagaContext {

    private final long memberId;
    private final String idempotencyKey;
    private final OrderCreateRequest request;

    @JsonIgnore
    @Setter
    private CartItemsResult cartItemsResult;

    @Setter
    private List<OrderLineItem> lineItems;

    @Setter
    private AmountResult amounts;

    @JsonIgnore
    @Setter
    private DeliveryAddressResponse address;

    @Setter
    private Long orderId;

    @Setter
    private String orderNo;

    @Setter
    private OrderStatus orderStatus;

    @Setter
    private List<Long> reservedCouponIds;
}
