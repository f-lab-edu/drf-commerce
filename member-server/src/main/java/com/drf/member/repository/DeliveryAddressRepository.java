package com.drf.member.repository;

import com.drf.member.entitiy.DeliveryAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeliveryAddressRepository extends JpaRepository<DeliveryAddress, Long> {

    boolean existsByMemberId(Long memberId);

    Optional<DeliveryAddress> findByMemberIdAndIsDefaultTrue(Long memberId);

    List<DeliveryAddress> findByMemberIdOrderByIdDesc(Long memberId);

    Optional<DeliveryAddress> findByIdAndMemberId(Long addressId, Long memberId);
}
