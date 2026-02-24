package com.systems.order.service;

import com.systems.order.client.PaymentClient;
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

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentClient paymentClient;
    private final OrderRepository orderRepository;

    /*
        @Retry uses Spring AOP proxies, it should not be called from the same service
     */
    @Retry(name = "paymentRetry", fallbackMethod = "paymentFallback")
    public PaymentResponse processPaymentWithRetry(Order order) {
        log.info("Attempting payment for order: {}", order.getId());
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
            order.setStatus(OrderStatus.FAILED);
            throw new BusinessException(ErrorCode.PAYMENT_SERVICE_UNAVAILABLE, "Payment service call failed");
        }
    }

    private PaymentResponse paymentFallback(Order order, Throwable t) {
        log.error("Payment fallback triggered for order: {} after max retries", order.getId());
        throw new BusinessException(ErrorCode.PAYMENT_FAILED, "Payment failed after maximum retries");
    }
}
