package com.systems.order.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    INVALID_ORDER_REQUEST("ORD_001", "Invalid order request"),
    ORDER_NOT_FOUND("ORD_002", "Order not found"),
    PAYMENT_FAILED("ORD_003", "Payment processing failed"),
    PAYMENT_SERVICE_UNAVAILABLE("ORD_004", "Payment service unavailable"),
    INTERNAL_ERROR("ORD_999", "Internal server error");
    
    private final String code;
    private final String message;
    
    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
