package com.systems.order.controller;

import com.systems.order.dto.OrderRequest;
import com.systems.order.dto.OrderResponse;
import com.systems.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@Slf4j
public class OrderController {
    private final OrderService orderService;
    
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }
    
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public List<OrderResponse> getAllOrders() {
        log.info("Request to get all orders");
        return orderService.getAllOrders();
    }
    
    @PostMapping("/process")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public OrderResponse processOrder(@RequestBody OrderRequest request) {
        log.info("Request to process order for customer: {}", request.customerId());
        return orderService.processOrder(request);
    }
}
