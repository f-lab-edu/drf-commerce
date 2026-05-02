package com.drf.coupon.entity;

import com.drf.common.converter.MoneyConverter;
import com.drf.common.entity.BaseTimeEntity;
import com.drf.common.exception.BusinessException;
import com.drf.common.model.Money;
import com.drf.coupon.common.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "coupon")
public class Coupon extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DiscountType discountType;

    @Column(nullable = false)
    private long discountValue;

    @Column(nullable = false)
    private int totalQuantity;

    @Column(nullable = false)
    private int issuedQuantity;

    @Convert(converter = MoneyConverter.class)
    @Column(nullable = false)
    private Money minOrderAmount;

    @Column(nullable = false)
    private int minOrderQuantity;

    @Convert(converter = MoneyConverter.class)
    private Money maxDiscountAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApplyType applyType;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ApplyScope applyScope;

    private Long applyTargetId;

    @Column(nullable = false)
    private boolean isUnlimited;

    @Column(nullable = false)
    private int maxIssuablePerMember;

    @Column(nullable = false)
    private LocalDateTime validFrom;

    @Column(nullable = false)
    private LocalDateTime validUntil;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CouponStatus status;

    private LocalDateTime deletedAt;

    public static Coupon create(String name, DiscountType discountType, long discountValue,
                                int totalQuantity, long minOrderAmount, int minOrderQuantity,
                                Long maxDiscountAmount, ApplyType applyType, ApplyScope applyScope,
                                Long applyTargetId, boolean isUnlimited, int maxIssuablePerMember,
                                LocalDateTime validFrom, LocalDateTime validUntil) {
        return Coupon.builder()
                .name(name)
                .discountType(discountType)
                .discountValue(discountValue)
                .totalQuantity(totalQuantity)
                .issuedQuantity(0)
                .minOrderAmount(Money.of(minOrderAmount))
                .minOrderQuantity(minOrderQuantity)
                .maxDiscountAmount(maxDiscountAmount != null ? Money.of(maxDiscountAmount) : null)
                .applyType(applyType)
                .applyScope(applyScope)
                .applyTargetId(applyTargetId)
                .isUnlimited(isUnlimited)
                .maxIssuablePerMember(maxIssuablePerMember)
                .validFrom(validFrom)
                .validUntil(validUntil)
                .status(CouponStatus.ACTIVE)
                .build();
    }

    public void delete() {
        this.status = CouponStatus.DELETED;
        this.deletedAt = LocalDateTime.now();
    }

    public void update(String name, DiscountType discountType, long discountValue,
                       int totalQuantity, long minOrderAmount, int minOrderQuantity,
                       Long maxDiscountAmount, ApplyType applyType, ApplyScope applyScope,
                       Long applyTargetId, boolean isUnlimited, int maxIssuablePerMember,
                       LocalDateTime validFrom, LocalDateTime validUntil) {
        this.name = name;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.totalQuantity = totalQuantity;
        this.minOrderAmount = Money.of(minOrderAmount);
        this.minOrderQuantity = minOrderQuantity;
        this.maxDiscountAmount = maxDiscountAmount != null ? Money.of(maxDiscountAmount) : null;
        this.applyType = applyType;
        this.applyScope = applyScope;
        this.applyTargetId = applyTargetId;
        this.isUnlimited = isUnlimited;
        this.maxIssuablePerMember = maxIssuablePerMember;
        this.validFrom = validFrom;
        this.validUntil = validUntil;
    }

    public void validateCouponAvailability() {
        if (status != CouponStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.COUPON_STATUS_INVALID);
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(validFrom) || now.isAfter(validUntil)) {
            throw new BusinessException(ErrorCode.COUPON_PERIOD_INVALID);
        }
    }

    public Long maxDiscountAmountValue() {
        return maxDiscountAmount != null ? maxDiscountAmount.toLong() : null;
    }

    public Long minOrderAmountValue() {
        return minOrderAmount != null ? minOrderAmount.toLong() : null;
    }

    public Money applyMaxCap(Money discount) {
        return maxDiscountAmount != null ? discount.min(maxDiscountAmount) : discount;
    }
}
