package com.drf.coupon.validation;

import org.springframework.stereotype.Component;

@Component
public class CouponAvailabilityValidator implements CouponValidationStrategy {

    @Override
    public boolean supports(ValidationType type) {
        return true;
    }

    @Override
    public void validate(CouponValidationContext context) {
        context.coupon().validateCouponAvailability();
    }
}
