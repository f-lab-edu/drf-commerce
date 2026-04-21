package com.drf.order.common.exception;

import com.drf.common.exception.errorcode.ErrorCodeSpec;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode implements ErrorCodeSpec {

    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 상품입니다."),
    PRODUCT_NOT_AVAILABLE(HttpStatus.BAD_REQUEST, "장바구니에 추가할 수 없는 상품입니다."),
    CART_NOT_FOUND(HttpStatus.NOT_FOUND, "장바구니가 존재하지 않습니다."),
    CART_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "장바구니에 없는 상품입니다."),
    COUPON_NOT_APPLICABLE(HttpStatus.BAD_REQUEST, "적용할 수 없는 쿠폰입니다."),
    ORDER_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "주문 금액이 일치하지 않습니다. 주문서를 다시 확인해주세요."),
    ORDER_STOCK_INSUFFICIENT(HttpStatus.CONFLICT, "재고가 부족하여 주문을 처리할 수 없습니다."),
    ORDER_COUPON_UNAVAILABLE(HttpStatus.CONFLICT, "쿠폰 선점에 실패하였습니다."),
    ORDER_PAYMENT_FAILED(HttpStatus.BAD_REQUEST, "결제에 실패하였습니다."),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."),
    SHIPPING_ADDRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "배송지를 찾을 수 없습니다."),
    CART_ITEM_NOT_OWNED(HttpStatus.BAD_REQUEST, "유효하지 않은 장바구니 상품이 포함되어 있습니다."),
    ;

    private final HttpStatus status;
    private final String message;
}
