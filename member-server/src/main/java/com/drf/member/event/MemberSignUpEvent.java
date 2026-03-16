package com.drf.member.event;

import com.drf.common.event.BaseEvent;

public class MemberSignUpEvent extends BaseEvent<MemberSignUpEvent.Payload> {

    public MemberSignUpEvent(Long id) {
        super(MemberEventType.MEMBER_SIGN_UP.name(), new MemberSignUpEvent.Payload(id));
    }

    public record Payload(long id) {
    }
}
