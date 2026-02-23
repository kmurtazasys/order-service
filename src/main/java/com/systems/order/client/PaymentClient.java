package com.systems.order.client;

import com.systems.order.dto.PaymentRequest;
import com.systems.order.dto.PaymentResponse;
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
        return restClient.post()
            .uri("/api/payments/process")
            .body(request)
            .retrieve()
            .body(PaymentResponse.class);
    }
}
