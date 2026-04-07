package com.drf.coupon.validation;

import com.drf.common.exception.BusinessException;
import com.drf.coupon.common.exception.ErrorCode;
import org.springframework.stereotype.Component;

@Component
public class MinOrderAmountValidator implements CouponValidationStrategy {

    @Override
    public boolean supports(ValidationType type) {
        return type == ValidationType.CALCULATE;
    }

    @Override
    public void validate(CouponValidationContext context) {
        if (context.orderAmount() < context.coupon().getMinOrderAmount()) {
            throw new BusinessException(ErrorCode.COUPON_MIN_ORDER_AMOUNT_NOT_MET);
        }
    }
}
