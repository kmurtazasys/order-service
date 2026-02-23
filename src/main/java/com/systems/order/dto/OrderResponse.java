package com.systems.order.dto;

import com.systems.order.entity.OrderStatus;
import java.math.BigDecimal;

public record OrderResponse(Long id, String customerId, String productId, Integer quantity, 
                           BigDecimal amount, OrderStatus status, String paymentId) {}
