package com.drf.order.facade;

import com.drf.common.exception.BusinessException;
import com.drf.common.model.AuthInfo;
import com.drf.order.client.CouponClient;
import com.drf.order.client.MemberClient;
import com.drf.order.client.PaymentClient;
import com.drf.order.client.ProductClient;
import com.drf.order.client.dto.request.*;
import com.drf.order.client.dto.response.*;
import com.drf.order.common.exception.ErrorCode;
import com.drf.order.entity.Cart;
import com.drf.order.entity.CartItem;
import com.drf.order.entity.Order;
import com.drf.order.model.dto.AmountResult;
import com.drf.order.model.dto.CartItemsResult;
import com.drf.order.model.dto.OrderLineItem;
import com.drf.order.model.request.OrderCreateRequest;
import com.drf.order.model.response.OrderCreateResponse;
import com.drf.order.service.CartService;
import com.drf.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderFacade {

    private static final int FREE_SHIPPING_THRESHOLD = 50_000;
    private static final int SHIPPING_FEE = 3_000;

    private final CartService cartService;
    private final ProductClient productClient;
    private final CouponClient couponClient;
    private final MemberClient memberClient;
    private final PaymentClient paymentClient;
    private final OrderService orderService;


    public OrderCreateResponse createOrder(AuthInfo authInfo, String idempotencyKey, OrderCreateRequest request) {
        long memberId = authInfo.id();

        // 1. 장바구니 상품 조회 + 소유권 검증
        CartItemsResult cartResult = cartService.getValidatedCartItems(memberId, request.cartItemIds());
        Cart cart = cartResult.cart();

        // 2. OrderLineItem 빌드 (상품 조회)
        List<OrderLineItem> lineItems = buildLineItems(cartResult.items());

        // 3. 상품쿠폰 할인 계산
        applyProductCouponDiscounts(memberId, lineItems);

        // 4. 카트쿠폰 할인 적용
        applyCartCouponDiscount(memberId, cart, lineItems);

        // 5. 금액 계산 + 기대금액 검증
        AmountResult amounts = calculateAmounts(lineItems);
        if (amounts.finalAmount() != request.expectedAmount()) {
            throw new BusinessException(ErrorCode.ORDER_AMOUNT_MISMATCH);
        }

        // 6. 배송지 조회
        DeliveryAddressResponse address;
        try {
            address = memberClient.getDeliveryAddress(memberId, request.shippingAddressId()).getData();
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SHIPPING_ADDRESS_NOT_FOUND);
        }

        // 7. 주문 생성 (PENDING)
        Order order = orderService.createOrder(memberId,
                lineItems.stream().map(OrderLineItem::toOrderItemData).toList(),
                address, cart.getCouponId(),
                amounts.deliveryFee(), amounts.totalAmount(), amounts.productDiscountAmount(),
                amounts.couponDiscountAmount(), amounts.finalAmount());

        // 8. 재고 선점
        List<OrderLineItem> reservedItems = new ArrayList<>();
        for (OrderLineItem item : lineItems) {
            String reserveKey = idempotencyKey + ":RESERVE:" + item.getProductId();
            try {
                productClient.reserveStock(item.getProductId(), reserveKey,
                        new StockReserveRequest(item.getQuantity()));
                reservedItems.add(item);
            } catch (Exception e) {
                log.error("Stock reserve failed for productId={}", item.getProductId(), e);
                reservedItems.forEach(ri -> releaseStockWithRetry(ri, idempotencyKey));
                orderService.failOrder(order.getId());
                throw new BusinessException(ErrorCode.ORDER_STOCK_INSUFFICIENT);
            }
        }

        // 9. 쿠폰 선점
        List<Long> allMemberCouponIds = buildCouponIds(cart.getCouponId(), lineItems);
        List<Long> reservedCoupons = new ArrayList<>();
        for (Long mcId : allMemberCouponIds) {
            try {
                couponClient.reserveCoupon(mcId, new CouponReserveRequest(memberId));
                reservedCoupons.add(mcId);
            } catch (Exception e) {
                log.error("Coupon reserve failed for memberCouponId={}", mcId, e);
                reservedCoupons.forEach(rc -> releaseCouponWithRetry(rc, memberId));
                lineItems.forEach(item -> releaseStockWithRetry(item, idempotencyKey));
                orderService.failOrder(order.getId());
                throw new BusinessException(ErrorCode.ORDER_COUPON_UNAVAILABLE);
            }
        }

        // 10. 결제
        try {
            paymentClient.pay(new PaymentRequest(order.getId(), amounts.finalAmount(), request.paymentMethodId()));
        } catch (Exception e) {
            log.error("Payment failed for orderId={}", order.getId(), e);
            reservedCoupons.forEach(rc -> releaseCouponWithRetry(rc, memberId));
            lineItems.forEach(item -> releaseStockWithRetry(item, idempotencyKey));
            orderService.failOrder(order.getId());
            throw new BusinessException(ErrorCode.ORDER_PAYMENT_FAILED);
        }

        // 11. 결제 완료 처리 (트랜잭션 커밋 후 Kafka ORDER_PAID 이벤트 자동 발행)
        orderService.completePayment(order.getId(), memberId,
                lineItems.stream().map(OrderLineItem::getCartItemId).toList(),
                allMemberCouponIds);

        return new OrderCreateResponse(order.getId(), order.getOrderNo(), order.getStatus().name(), amounts.finalAmount());
    }

    private List<OrderLineItem> buildLineItems(List<CartItem> cartItems) {
        List<Long> productIds = cartItems.stream().map(CartItem::getProductId).toList();

        Map<Long, InternalProductResponse> productMap = new HashMap<>();
        ProductBatchRequest productBatchRequest = new ProductBatchRequest(productIds);
        for (InternalProductResponse p : productClient.getProductsBatch(productBatchRequest).getData()) {
            productMap.put(p.id(), p);
        }

        List<OrderLineItem> lineItems = new ArrayList<>();
        for (CartItem item : cartItems) {
            lineItems.add(OrderLineItem.of(item, productMap.get(item.getProductId())));
        }
        return lineItems;
    }

    private void applyProductCouponDiscounts(long memberId, List<OrderLineItem> lineItems) {
        for (OrderLineItem lineItem : lineItems) {
            if (lineItem.getMemberCouponId() == null) continue;
            InternalProductCouponRequest req = new InternalProductCouponRequest(
                    memberId, lineItem.getCartItemId(), lineItem.getProductId(),
                    lineItem.getDiscountedPrice(), lineItem.getQuantity(),
                    lineItem.getCategoryPath(), List.of());
            ProductCouponCalculateResponse r = couponClient
                    .calculateProductCoupon(lineItem.getMemberCouponId(), req).getData();
            lineItem.applyProductCouponDiscount(r.applicable() ? r.discountAmount() : 0);
        }
    }

    private void applyCartCouponDiscount(long memberId, Cart cart, List<OrderLineItem> lineItems) {
        if (cart.getCouponId() == null) return;
        List<InternalCartCouponItemRequest> couponItems = lineItems.stream()
                .map(item -> new InternalCartCouponItemRequest(
                        item.getCartItemId(), item.getProductId(), item.getDiscountedPrice(),
                        item.getQuantity(), item.getCategoryPath()))
                .toList();
        InternalCartCouponCalculateResponse r = couponClient
                .calculateCartCoupon(cart.getCouponId(), new InternalCartCouponRequest(memberId, couponItems))
                .getData();
        if (!r.applicable()) return;
        Map<Long, Integer> discountByItem = new HashMap<>();
        for (InternalCouponItemResult result : r.items()) {
            discountByItem.put(result.cartItemId(), result.discountAmount());
        }
        for (OrderLineItem item : lineItems) {
            item.applyOrderCouponDiscount(discountByItem.getOrDefault(item.getCartItemId(), 0));
        }
    }

    private AmountResult calculateAmounts(List<OrderLineItem> lineItems) {
        int totalAmount = lineItems.stream().mapToInt(OrderLineItem::subtotal).sum();
        int productDiscountAmount = lineItems.stream()
                .mapToInt(item -> (item.getUnitPrice() - item.getDiscountedPrice()) * item.getQuantity()).sum();
        int couponDiscountAmount = lineItems.stream().mapToInt(OrderLineItem::totalCouponDiscount).sum();
        int netAmount = totalAmount - couponDiscountAmount;
        int deliveryFee = netAmount >= FREE_SHIPPING_THRESHOLD ? 0 : SHIPPING_FEE;
        return new AmountResult(totalAmount, productDiscountAmount, couponDiscountAmount, deliveryFee, netAmount + deliveryFee);
    }

    private List<Long> buildCouponIds(Long cartCouponId, List<OrderLineItem> lineItems) {
        List<Long> ids = new ArrayList<>();
        if (cartCouponId != null) ids.add(cartCouponId);
        for (OrderLineItem item : lineItems) {
            if (item.getMemberCouponId() != null && !ids.contains(item.getMemberCouponId())) {
                ids.add(item.getMemberCouponId());
            }
        }
        return ids;
    }

    private void releaseStockWithRetry(OrderLineItem item, String idempotencyKey) {
        String releaseKey = idempotencyKey + ":RELEASE:" + item.getProductId();
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                productClient.releaseStock(item.getProductId(), releaseKey,
                        new StockReleaseRequest(item.getQuantity()));
                return;
            } catch (Exception e) {
                log.warn("Stock release failed attempt {}/3 for productId={}: {}", attempt, item.getProductId(), e.getMessage());
            }
        }
        log.error("Stock release exhausted retries for productId={}", item.getProductId());
    }

    private void releaseCouponWithRetry(long memberCouponId, long memberId) {
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                couponClient.releaseCoupon(memberCouponId, new CouponReserveRequest(memberId));
                return;
            } catch (Exception e) {
                log.warn("Coupon release failed attempt {}/3 for memberCouponId={}: {}", attempt, memberCouponId, e.getMessage());
            }
        }
        log.error("Coupon release exhausted retries for memberCouponId={}", memberCouponId);
    }
}
