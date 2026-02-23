package com.systems.order.service;

import com.systems.order.client.PaymentClient;
import com.systems.order.dto.OrderRequest;
import com.systems.order.dto.OrderResponse;
import com.systems.order.entity.Order;
import com.systems.order.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final PaymentClient paymentClient;
    private final OrderSagaOrchestrator sagaOrchestrator;
    
    public OrderService(OrderRepository orderRepository, PaymentClient paymentClient, 
                       OrderSagaOrchestrator sagaOrchestrator) {
        this.orderRepository = orderRepository;
        this.paymentClient = paymentClient;
        this.sagaOrchestrator = sagaOrchestrator;
    }
    
    @Cacheable("orders")
    public List<OrderResponse> getAllOrders() {
        log.info("Fetching all orders");
        List<OrderResponse> orders = orderRepository.findAll().stream()
            .map(this::toResponse)
            .toList();
        log.debug("Found {} orders", orders.size());
        return orders;
    }
    
    @Transactional
    public OrderResponse processOrder(OrderRequest request) {
        log.info("Processing order for customer: {}", request.customerId());
        try {
            validateRequest(request);
            OrderResponse response = sagaOrchestrator.executeOrderSaga(request);
            log.info("Order processed successfully: {}", response.id());
            return response;
        } catch (Exception ex) {
            log.error("Failed to process order for customer: {}", request.customerId(), ex);
            throw ex;
        }
    }
    
    private void validateRequest(OrderRequest request) {
        log.debug("Validating order request");
        if (request.customerId() == null || request.customerId().isBlank()) {
            throw new IllegalArgumentException("Customer ID is required");
        }
        if (request.productId() == null || request.productId().isBlank()) {
            throw new IllegalArgumentException("Product ID is required");
        }
        if (request.quantity() == null || request.quantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (request.amount() == null || request.amount().signum() <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        log.debug("Order request validated successfully");
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
