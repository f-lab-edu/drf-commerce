package com.drf.coupon.validation;

import com.drf.common.exception.BusinessException;
import com.drf.coupon.common.exception.ErrorCode;
import com.drf.coupon.entity.ApplyType;
import org.springframework.stereotype.Component;

@Component
public class CategoryAmountValidator implements CouponValidationStrategy {

    @Override
    public boolean supports(ValidationType type) {
        return type == ValidationType.CALCULATE;
    }

    @Override
    public void validate(CouponValidationContext context) {
        if (context.coupon().getApplyType() == ApplyType.CATEGORY
                && (context.categoryAmount() == null || context.categoryAmount() == 0)) {
            throw new BusinessException(ErrorCode.CATEGORY_AMOUNT_REQUIRED);
        }
    }
}
