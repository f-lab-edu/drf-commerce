package com.drf.coupon.validation;

public interface CouponValidationStrategy {

    boolean supports(ValidationType type);

    void validate(CouponValidationContext context);
}
