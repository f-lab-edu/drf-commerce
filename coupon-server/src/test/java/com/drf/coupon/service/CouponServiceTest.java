package com.drf.coupon.service;

import com.drf.coupon.entity.*;
import com.drf.coupon.model.response.MemberCouponListResponse;
import com.drf.coupon.repository.MemberCouponRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @InjectMocks
    private CouponService couponService;

    @Mock
    private MemberCouponRepository memberCouponRepository;

    @Nested
    @DisplayName("보유 쿠폰 목록 조회")
    class GetMemberCoupons {

        @Test
        @DisplayName("보유 쿠폰 목록 반환")
        void getMemberCoupons_success() {
            // given
            Coupon coupon = Coupon.builder()
                    .id(1L)
                    .name("신규 가입 쿠폰")
                    .discountType(DiscountType.FIXED)
                    .discountValue(3000)
                    .totalQuantity(100)
                    .issuedQuantity(1)
                    .minOrderAmount(10000)
                    .applyType(ApplyType.ALL)
                    .validFrom(LocalDateTime.of(2026, 4, 1, 0, 0))
                    .validUntil(LocalDateTime.of(2026, 4, 30, 23, 59))
                    .status(CouponStatus.ACTIVE)
                    .build();

            MemberCoupon memberCoupon = MemberCoupon.builder()
                    .id(1L)
                    .coupon(coupon)
                    .memberId(1L)
                    .status(MemberCouponStatus.UNUSED)
                    .build();

            given(memberCouponRepository.findByMemberIdAndStatus(1L, MemberCouponStatus.UNUSED)).willReturn(List.of(memberCoupon));

            // when
            List<MemberCouponListResponse> result = couponService.getMemberCoupons(1L);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).memberCouponId()).isEqualTo(1L);
            assertThat(result.get(0).couponName()).isEqualTo("신규 가입 쿠폰");
            assertThat(result.get(0).status()).isEqualTo(MemberCouponStatus.UNUSED);
        }

        @Test
        @DisplayName("보유 쿠폰이 없으면 빈 목록 반환")
        void getMemberCoupons_empty() {
            // given
            given(memberCouponRepository.findByMemberIdAndStatus(1L, MemberCouponStatus.UNUSED)).willReturn(List.of());

            // when
            List<MemberCouponListResponse> result = couponService.getMemberCoupons(1L);

            // then
            assertThat(result).isEmpty();
        }
    }
}
