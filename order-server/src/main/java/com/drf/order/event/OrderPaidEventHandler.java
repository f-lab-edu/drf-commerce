package com.drf.order.event;

import com.drf.common.event.EventTopic;
import com.drf.common.infrastructure.kafka.KafkaProducer;
import com.drf.common.util.JsonConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPaidEventHandler {

    private final KafkaProducer kafkaProducer;
    private final JsonConverter jsonConverter;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(OrderPaidApplicationEvent event) {
        OrderPaidEvent kafkaEvent = new OrderPaidEvent(
                new OrderPaidEventPayload(event.orderId(), event.memberId(), event.cartItemIds(), event.usedMemberCouponIds()));
        kafkaProducer.sendMessage(
                EventTopic.ORDER.getName(),
                String.valueOf(event.orderId()),
                jsonConverter.toJson(kafkaEvent),
                () -> log.error("Failed to send ORDER_PAID event for orderId={}", event.orderId()));
    }
}
