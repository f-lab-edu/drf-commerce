package com.drf.member.repository;

import com.drf.member.entitiy.WithdrawnMemberHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface WithdrawnMemberHistoryRepository extends JpaRepository<WithdrawnMemberHistory, Long> {
    boolean existsByEmailAndRejoinAllowedAtAfter(String email, LocalDate now);
}
