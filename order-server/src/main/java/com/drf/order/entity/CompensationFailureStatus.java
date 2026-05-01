package com.drf.order.entity;

public enum CompensationFailureStatus {
    PENDING,    // 기록됨, 재처리 대기
    RETRYING,   // 재처리 진행 중
    RESOLVED,   // 재처리 성공
    ABANDONED   // 재시도 소진, 수동 처리 필요
}
