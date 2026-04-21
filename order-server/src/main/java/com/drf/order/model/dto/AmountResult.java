package com.drf.order.model.dto;

public record AmountResult(int totalAmount, int productDiscountAmount, int couponDiscountAmount, int deliveryFee,
                           int finalAmount) {
}
