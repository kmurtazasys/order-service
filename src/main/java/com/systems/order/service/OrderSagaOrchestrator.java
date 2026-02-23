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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class OrderSagaOrchestrator {
    private final OrderRepository orderRepository;
    private final PaymentClient paymentClient;
    
    public OrderSagaOrchestrator(OrderRepository orderRepository, PaymentClient paymentClient) {
        this.orderRepository = orderRepository;
        this.paymentClient = paymentClient;
    }
    
    @Transactional
    public OrderResponse executeOrderSaga(OrderRequest request) {
        log.info("Starting SAGA orchestration for order");
        
        // Step 1: Create Order
        Order order = createOrder(request);
        log.info("Order created with ID: {}", order.getId());
        
        // Step 2: Process Payment with Retry
        try {
            PaymentResponse paymentResponse = processPaymentWithRetry(order);
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
    
    private Order createOrder(OrderRequest request) {
        log.debug("Creating order entity");
        Order order = new Order();
        order.setCustomerId(request.customerId());
        order.setProductId(request.productId());
        order.setQuantity(request.quantity());
        order.setAmount(request.amount());
        order.setStatus(OrderStatus.PENDING);
        return orderRepository.save(order);
    }
    
    @Retry(name = "paymentRetry", fallbackMethod = "paymentFallback")
    private PaymentResponse processPaymentWithRetry(Order order) {
        log.info("Attempting payment for order: {}", order.getId());
        order.setStatus(OrderStatus.FAILED);
        orderRepository.save(order);
        
        try {
            PaymentRequest paymentRequest = new PaymentRequest(
                order.getId(),
                order.getAmount(),
                order.getCustomerId()
            );
            PaymentResponse response = paymentClient.processPayment(paymentRequest);
            log.info("Payment processed successfully for order: {}", order.getId());
            return response;
        } catch (Exception ex) {
            log.warn("Payment attempt failed for order: {}, will retry", order.getId());
            throw new BusinessException(ErrorCode.PAYMENT_SERVICE_UNAVAILABLE, "Payment service call failed");
        }
    }
    
    private PaymentResponse paymentFallback(Order order, Exception e) {
        log.error("Payment fallback triggered for order: {} after max retries", order.getId());
        throw new BusinessException(ErrorCode.PAYMENT_FAILED, "Payment failed after maximum retries");
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
