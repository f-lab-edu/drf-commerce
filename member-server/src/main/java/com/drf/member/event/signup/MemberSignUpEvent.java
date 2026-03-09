package com.drf.member.event.signup;

import com.drf.member.event.base.BaseEvent;
import com.drf.member.event.base.EventType;

public class MemberSignUpEvent extends BaseEvent<MemberSignUpEventPayload> {

    public MemberSignUpEvent(Long id) {
        super(EventType.MEMBER_SIGN_UP, new MemberSignUpEventPayload(id));
    }
}


