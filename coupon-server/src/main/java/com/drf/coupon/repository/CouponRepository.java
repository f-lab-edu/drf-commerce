package com.drf.coupon.repository;

import com.drf.coupon.entity.Coupon;
import com.drf.coupon.entity.CouponStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    Optional<Coupon> findByIdAndStatusNot(Long id, CouponStatus status);
}
