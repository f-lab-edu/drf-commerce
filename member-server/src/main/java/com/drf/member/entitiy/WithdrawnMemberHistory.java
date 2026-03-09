package com.drf.member.entitiy;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "withdrawn_member_history")
public class WithdrawnMemberHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false)
    private LocalDate withdrawnAt;

    @Column(nullable = false)
    private LocalDate rejoinAllowedAt;

    public static WithdrawnMemberHistory create(String email, LocalDate withdrawnAt, LocalDate rejoinAllowedAt) {
        return WithdrawnMemberHistory.builder()
                .email(email)
                .withdrawnAt(withdrawnAt)
                .rejoinAllowedAt(rejoinAllowedAt)
                .build();
    }
}
