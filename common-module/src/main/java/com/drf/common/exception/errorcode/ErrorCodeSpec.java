package com.drf.common.exception.errorcode;

import org.springframework.http.HttpStatus;

public interface ErrorCodeSpec {
    String name();

    HttpStatus getStatus();

    String getMessage();
}
