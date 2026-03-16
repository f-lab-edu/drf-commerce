package com.drf.common.exception;

import com.drf.common.exception.errorcode.ErrorCodeSpec;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final ErrorCodeSpec errorCode;

    public BusinessException(ErrorCodeSpec errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
