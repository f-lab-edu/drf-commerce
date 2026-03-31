package com.drf.coupon.controller;

import com.drf.coupon.entity.ApplyType;
import com.drf.coupon.entity.DiscountType;
import com.drf.coupon.entity.MemberCouponStatus;
import com.drf.coupon.model.response.MemberCouponListResponse;
import com.drf.coupon.service.CouponService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CouponController.class)
class CouponControllerTest extends BaseControllerTest {

    @MockitoBean
    private CouponService couponService;

    @Nested
    @DisplayName("보유 쿠폰 목록 조회")
    class GetMemberCoupons {

        @Test
        @DisplayName("조회 성공")
        void getMemberCoupons_success() throws Exception {
            MemberCouponListResponse response = new MemberCouponListResponse(
                    1L, "신규 가입 쿠폰", DiscountType.FIXED, 3000, null, 10000,
                    ApplyType.ALL, null,
                    LocalDateTime.of(2026, 4, 1, 0, 0), LocalDateTime.of(2026, 4, 30, 23, 59),
                    MemberCouponStatus.UNUSED
            );

            given(couponService.getMemberCoupons(anyLong())).willReturn(List.of(response));

            mockMvc.perform(get("/members/me/coupons")
                            .header("X-User-Id", 1)
                            .header("X-User-Role", "USER"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].memberCouponId").value(1L))
                    .andExpect(jsonPath("$.data[0].couponName").value("신규 가입 쿠폰"))
                    .andExpect(jsonPath("$.data[0].status").value("UNUSED"));
        }

        @Test
        @DisplayName("보유 쿠폰이 없으면 빈 목록 반환")
        void getMemberCoupons_empty() throws Exception {
            given(couponService.getMemberCoupons(anyLong())).willReturn(List.of());

            mockMvc.perform(get("/members/me/coupons")
                            .header("X-User-Id", 1)
                            .header("X-User-Role", "USER"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isEmpty());
        }
    }
}
