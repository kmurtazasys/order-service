package com.systems.order.service;

import com.systems.order.client.PaymentClient;
import com.systems.order.dto.OrderRequest;
import com.systems.order.dto.OrderResponse;
import com.systems.order.dto.PaymentRequest;
import com.systems.order.dto.PaymentResponse;
import com.systems.order.entity.Order;
import com.systems.order.entity.OrderStatus;
import com.systems.order.exception.BusinessException;
import com.systems.order.exception.ErrorCode;
import com.systems.order.repository.OrderRepository;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderSagaOrchestrator {
    private final OrderRepository orderRepository;
    private final PaymentService paymentService;

    @Transactional
    public OrderResponse executeOrderSaga(OrderRequest request, Long userId) {
        log.info("Starting SAGA orchestration for order");
        
        // Step 1: Create Order
        Order order = createOrder(request, userId);
        log.info("Order created with ID: {}", order.getId());
        
        // Step 2: Process Payment with Retry
        try {
            PaymentResponse paymentResponse = paymentService.processPaymentWithRetry(order);
            order.setPaymentId(paymentResponse.paymentId());
            order.setStatus(OrderStatus.COMPLETED);
            log.info("Payment successful for order: {}, paymentId: {}", order.getId(), paymentResponse.paymentId());
        } catch (Exception e) {
            log.error("Payment failed after retries for order: {}", order.getId(), e);
            order.setStatus(OrderStatus.REVERSAL);
        }
        
        order = orderRepository.save(order);
        log.info("SAGA completed for order: {} with status: {}", order.getId(), order.getStatus());
        return toResponse(order);
    }
    
    private Order createOrder(OrderRequest request, Long userId) {
        log.debug("Creating order entity");
        Order order = new Order();
        order.setCustomerId(userId.toString());
        order.setProductId(request.productId());
        order.setQuantity(request.quantity());
        order.setAmount(request.amount());
        order.setStatus(OrderStatus.PENDING);
        return orderRepository.save(order);
    }
    
    private OrderResponse toResponse(Order order) {
        return new OrderResponse(
            order.getId(),
            order.getCustomerId(),
            order.getProductId(),
            order.getQuantity(),
            order.getAmount(),
            order.getStatus(),
            order.getPaymentId()
        );
    }
}
