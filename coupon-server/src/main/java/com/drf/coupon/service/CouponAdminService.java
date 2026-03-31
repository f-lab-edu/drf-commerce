package com.drf.coupon.service;

import com.drf.common.exception.BusinessException;
import com.drf.coupon.common.exception.ErrorCode;
import com.drf.coupon.entity.ApplyType;
import com.drf.coupon.entity.Coupon;
import com.drf.coupon.entity.CouponStatus;
import com.drf.coupon.entity.DiscountType;
import com.drf.coupon.model.request.CouponCreateRequest;
import com.drf.coupon.model.request.CouponUpdateRequest;
import com.drf.coupon.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CouponAdminService {

    private final CouponRepository couponRepository;

    @Transactional
    public Long createCoupon(CouponCreateRequest request) {
        validateCouponFields(request.discountType(), request.maxDiscountAmount(),
                request.applyType(), request.applyTargetId(), request.validFrom(), request.validUntil());

        Coupon coupon = Coupon.create(
                request.name(),
                request.discountType(),
                request.discountValue(),
                request.totalQuantity(),
                request.minOrderAmount(),
                request.maxDiscountAmount(),
                request.applyType(),
                request.applyTargetId(),
                request.validFrom(),
                request.validUntil()
        );

        return couponRepository.save(coupon).getId();
    }

    @Transactional
    public void updateCoupon(Long id, CouponUpdateRequest request) {
        validateCouponFields(request.discountType(), request.maxDiscountAmount(),
                request.applyType(), request.applyTargetId(), request.validFrom(), request.validUntil());

        Coupon coupon = couponRepository.findByIdAndStatusNot(id, CouponStatus.DELETED)
                .orElseThrow(() -> new BusinessException(ErrorCode.COUPON_NOT_FOUND));

        coupon.update(
                request.name(),
                request.discountType(),
                request.discountValue(),
                request.totalQuantity(),
                request.minOrderAmount(),
                request.maxDiscountAmount(),
                request.applyType(),
                request.applyTargetId(),
                request.validFrom(),
                request.validUntil()
        );
    }

    @Transactional
    public void deleteCoupon(Long id) {
        Coupon coupon = couponRepository.findByIdAndStatusNot(id, CouponStatus.DELETED)
                .orElseThrow(() -> new BusinessException(ErrorCode.COUPON_NOT_FOUND));
        coupon.delete();
    }

    private void validateCouponFields(DiscountType discountType, Integer maxDiscountAmount,
                                      ApplyType applyType, Long applyTargetId,
                                      LocalDateTime validFrom, LocalDateTime validUntil) {
        if (!validUntil.isAfter(validFrom)) {
            throw new BusinessException(ErrorCode.INVALID_VALID_DATE_RANGE);
        }
        if (applyType == ApplyType.CATEGORY && applyTargetId == null) {
            throw new BusinessException(ErrorCode.CATEGORY_COUPON_REQUIRES_TARGET);
        }
        if (discountType == DiscountType.RATE && maxDiscountAmount == null) {
            throw new BusinessException(ErrorCode.RATE_COUPON_REQUIRES_MAX_DISCOUNT);
        }
    }
}
