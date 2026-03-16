package com.drf.common.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventTopic {
    MEMBER("member");

    private final String name;
}