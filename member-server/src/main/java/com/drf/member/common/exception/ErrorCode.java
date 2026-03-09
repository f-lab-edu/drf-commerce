package com.drf.member.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    REJOIN_NOT_ALLOWED(HttpStatus.FORBIDDEN, "탈퇴 후 재가입이 불가한 기간입니다."),
    ;

    private final HttpStatus status;
    private final String message;
}
