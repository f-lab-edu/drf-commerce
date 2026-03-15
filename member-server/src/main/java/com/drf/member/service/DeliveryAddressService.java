package com.drf.member.service;

import com.drf.member.common.exception.BusinessException;
import com.drf.member.common.exception.ErrorCode;
import com.drf.member.common.model.AuthInfo;
import com.drf.member.entitiy.DeliveryAddress;
import com.drf.member.entitiy.Member;
import com.drf.member.model.request.DeliveryAddressCreateRequest;
import com.drf.member.repository.DeliveryAddressRepository;
import com.drf.member.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor

public class DeliveryAddressService {

    private final DeliveryAddressRepository deliveryAddressRepository;
    private final MemberRepository memberRepository;


    @Transactional
    public void register(DeliveryAddressCreateRequest request, AuthInfo authInfo) {
        Member member = memberRepository.findById(authInfo.id())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        boolean isDefault = request.isDefault() || !deliveryAddressRepository.existsByMember(member);

        if (isDefault) {
            deliveryAddressRepository.findByMemberAndIsDefaultTrue(member)
                    .ifPresent(DeliveryAddress::unmarkDefault);
        }

        DeliveryAddress deliveryAddress = DeliveryAddress.builder()
                .member(member)
                .name(request.name())
                .phone(request.phone())
                .address(request.address())
                .addressDetail(request.addressDetail())
                .zipCode(request.zipCode())
                .isDefault(isDefault)
                .build();

        deliveryAddressRepository.save(deliveryAddress);
    }
}
