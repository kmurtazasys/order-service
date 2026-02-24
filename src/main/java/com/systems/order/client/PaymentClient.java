package com.systems.order.client;

import com.systems.order.dto.PaymentRequest;
import com.systems.order.dto.PaymentResponse;
import com.systems.order.exception.BusinessException;
import com.systems.order.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentClient {
    @Value("${payment.service.url}")
    String paymentServiceUrl;

    private final WebClient webClient;

    public PaymentResponse processPayment(PaymentRequest request) {
        try {
            return webClient.post()
                    .uri("/api/v1/payments")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(PaymentResponse.class)
                    .block(); // Synchronous call
        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw new BusinessException(ErrorCode.PAYMENT_SERVICE_UNAVAILABLE, "Payment service call failed");
        }
    }
}
