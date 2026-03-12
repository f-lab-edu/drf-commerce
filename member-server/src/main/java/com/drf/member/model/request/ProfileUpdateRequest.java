package com.drf.member.model.request;

import com.drf.member.common.validation.annotation.ValidPhone;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileUpdateRequest {

    @Size(min = 2, max = 50)
    private String name;

    @ValidPhone
    private String phone;
}
