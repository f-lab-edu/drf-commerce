package com.drf.order.service;

import com.drf.common.util.JsonConverter;
import com.drf.order.entity.CompensationFailureStatus;
import com.drf.order.entity.SagaCompensationFailure;
import com.drf.order.repository.SagaCompensationFailureRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SagaCompensationFailureService {

    private final SagaCompensationFailureRepository repository;
    private final JsonConverter jsonConverter;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveFailure(String sagaName, String stepName, RuntimeException cause, Object snapshot) {
        try {
            String errorMessage = cause.getMessage() != null ? cause.getMessage() : cause.getClass().getSimpleName();
            String contextJson = snapshot != null ? jsonConverter.toJson(snapshot) : null;

            repository.save(SagaCompensationFailure.builder()
                    .sagaName(sagaName)
                    .stepName(stepName)
                    .errorMessage(errorMessage)
                    .contextSnapshot(contextJson)
                    .status(CompensationFailureStatus.PENDING)
                    .retryCount(0)
                    .build());
        } catch (Exception e) {
            log.error("Failed to record compensation failure. sagaName={}, stepName={}", sagaName, stepName, e);
        }
    }
}
