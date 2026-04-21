package com.drf.order.model.response;

public record OrderCreateResponse(long orderId, String orderNo, String status, int finalAmount) {
}
