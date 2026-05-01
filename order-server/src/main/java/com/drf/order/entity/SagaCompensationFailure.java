package com.drf.order.entity;

import com.drf.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "saga_compensation_failure")
public class SagaCompensationFailure extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String sagaName;

    @Column(nullable = false, length = 100)
    private String stepName;

    @Column(nullable = false, length = 500)
    private String errorMessage;

    @Column(columnDefinition = "TEXT")
    private String contextSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CompensationFailureStatus status;

    @Column(nullable = false)
    private int retryCount;

}
