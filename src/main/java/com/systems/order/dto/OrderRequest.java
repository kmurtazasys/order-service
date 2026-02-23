package com.systems.order.dto;

import java.math.BigDecimal;

public record OrderRequest(String customerId, String productId, Integer quantity, BigDecimal amount) {}
