package com.drf.member.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberSignUpRequest {
    private String email;
    private String password;
    private String name;
    private String phone;
    private LocalDate birthDate;
}
