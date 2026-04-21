package com.drf.order.model.dto;

import com.drf.order.client.dto.response.InternalProductResponse;
import com.drf.order.entity.CartItem;
import lombok.Getter;

import java.util.List;

@Getter
public class OrderLineItem {

    private final long cartItemId;
    private final long productId;
    private final String productName;
    private final int unitPrice;
    private final int discountedPrice;
    private final int quantity;
    private final Long memberCouponId;
    private final List<Long> categoryPath;
    private int productCouponDiscount;
    private int orderCouponDiscount;

    private OrderLineItem(long cartItemId, long productId, String productName,
                          int unitPrice, int discountedPrice, int quantity,
                          Long memberCouponId, List<Long> categoryPath) {
        this.cartItemId = cartItemId;
        this.productId = productId;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.discountedPrice = discountedPrice;
        this.quantity = quantity;
        this.memberCouponId = memberCouponId;
        this.categoryPath = categoryPath;
    }

    public static OrderLineItem of(CartItem cartItem, InternalProductResponse product) {
        return new OrderLineItem(
                cartItem.getId(), cartItem.getProductId(), product.name(),
                product.price(), product.discountedPrice(), cartItem.getQuantity(),
                cartItem.getCouponId(), product.categoryPath()
        );
    }

    public void applyProductCouponDiscount(int discount) {
        this.productCouponDiscount = discount;
    }

    public void applyOrderCouponDiscount(int discount) {
        this.orderCouponDiscount = discount;
    }

    public int subtotal() {
        return discountedPrice * quantity;
    }

    public int totalCouponDiscount() {
        return productCouponDiscount + orderCouponDiscount;
    }

    public int finalAmount() {
        return subtotal() - totalCouponDiscount();
    }

    public OrderItemData toOrderItemData() {
        return new OrderItemData(
                productId, productName, unitPrice, discountedPrice, quantity,
                productCouponDiscount, orderCouponDiscount, finalAmount(), memberCouponId
        );
    }
}
