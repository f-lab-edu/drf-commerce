package com.drf.product.common.exception;

import com.drf.common.exception.errorcode.ErrorCodeSpec;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode implements ErrorCodeSpec {

    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 카테고리입니다."),
    INVALID_SALE_DATE_RANGE(HttpStatus.BAD_REQUEST, "세일 기간이 올바르지 않습니다.")
    ;
    private final HttpStatus status;
    private final String message;
}
