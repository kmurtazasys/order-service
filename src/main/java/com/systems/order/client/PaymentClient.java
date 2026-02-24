package com.systems.order.client;

import com.systems.order.dto.PaymentRequest;
import com.systems.order.dto.PaymentResponse;
import com.systems.order.exception.BusinessException;
import com.systems.order.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class PaymentClient {
    private final RestClient restClient;
    
    public PaymentClient(@Value("${payment.service.url}") String paymentServiceUrl) {
        this.restClient = RestClient.builder()
            .baseUrl(paymentServiceUrl)
            .build();
    }
    
    public PaymentResponse processPayment(PaymentRequest request) {
        try {
            return restClient.post()
                    .uri("/api/payments/process")
                    .body(request)
                    .retrieve()
                    .body(PaymentResponse.class);
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.PAYMENT_SERVICE_UNAVAILABLE, "Payment service call failed");
        }

    }
}
