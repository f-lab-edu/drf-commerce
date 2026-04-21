package com.drf.order.model.dto;

import com.drf.order.entity.Cart;
import com.drf.order.entity.CartItem;

import java.util.List;

public record CartItemsResult(Cart cart, List<CartItem> items) {

}
