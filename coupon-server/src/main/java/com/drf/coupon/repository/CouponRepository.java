package com.drf.coupon.repository;

import com.drf.coupon.entity.Coupon;
import com.drf.coupon.entity.CouponStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    List<Coupon> findByStatusNot(CouponStatus status);

    Optional<Coupon> findByIdAndStatusNot(Long id, CouponStatus status);
}
