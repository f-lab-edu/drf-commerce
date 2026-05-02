package com.drf.inventory.common.exception;

import com.drf.common.exception.errorcode.ErrorCodeSpec;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode implements ErrorCodeSpec {

    CONFIRMED_STOCK_NOT_FOUND(HttpStatus.NOT_FOUND, "확정 재고 정보를 찾을 수 없습니다."),
    AVAILABLE_STOCK_NOT_FOUND(HttpStatus.NOT_FOUND, "가용 재고 정보를 찾을 수 없습니다."),
    INSUFFICIENT_AVAILABLE_STOCK(HttpStatus.CONFLICT, "가용 재고가 부족합니다."),
    NEGATIVE_STOCK_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "재고는 음수가 될 수 없습니다.")
    ;
    private final HttpStatus status;
    private final String message;
}
