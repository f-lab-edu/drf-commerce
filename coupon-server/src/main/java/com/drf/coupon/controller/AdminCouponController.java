package com.drf.coupon.controller;

import com.drf.common.model.CommonResponse;
import com.drf.coupon.model.request.CouponCreateRequest;
import com.drf.coupon.model.request.CouponUpdateRequest;
import com.drf.coupon.model.response.CouponCreateResponse;
import com.drf.coupon.service.CouponAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
public class AdminCouponController {

    private final CouponAdminService couponAdminService;

    @PostMapping("/admin/coupons")
    public ResponseEntity<CommonResponse<CouponCreateResponse>> createCoupon(
            @Valid @RequestBody CouponCreateRequest request) {
        Long couponId = couponAdminService.createCoupon(request);
        return ResponseEntity.ok(CommonResponse.success(new CouponCreateResponse(couponId)));
    }

    @PutMapping("/admin/coupons/{id}")
    public ResponseEntity<Void> updateCoupon(
            @PathVariable Long id, @Valid @RequestBody CouponUpdateRequest request) {
        couponAdminService.updateCoupon(id, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/admin/coupons/{id}")
    public ResponseEntity<Void> deleteCoupon(@PathVariable Long id) {
        couponAdminService.deleteCoupon(id);
        return ResponseEntity.noContent().build();
    }
}
