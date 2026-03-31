package com.drf.coupon.service;

import com.drf.coupon.entity.MemberCouponStatus;
import com.drf.coupon.model.response.MemberCouponListResponse;
import com.drf.coupon.repository.MemberCouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final MemberCouponRepository memberCouponRepository;

    @Transactional(readOnly = true)
    public List<MemberCouponListResponse> getMemberCoupons(Long memberId) {
        return memberCouponRepository.findByMemberIdAndStatus(memberId, MemberCouponStatus.UNUSED).stream()
                .map(MemberCouponListResponse::from)
                .toList();
    }
}
