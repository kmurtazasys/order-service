package com.systems.order.service;

import com.systems.order.dto.OrderRequest;
import com.systems.order.entity.Order;
import com.systems.order.entity.OrderStatus;
import com.systems.order.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    
    @Mock
    private OrderRepository orderRepository;
    
    @Mock
    private OrderSagaOrchestrator sagaOrchestrator;
    
    @InjectMocks
    private OrderService orderService;
    
    @Test
    void getAllOrders_Success() {
        Order order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.COMPLETED);
        
        when(orderRepository.findAll()).thenReturn(List.of(order));
        
        var result = orderService.getAllOrders();
        
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(orderRepository).findAll();
    }
    
    @Test
    void processOrder_ValidRequest() {
        OrderRequest request = new OrderRequest("prod1", 2, BigDecimal.TEN);
        
        orderService.processOrder(request, 1L);
        
        verify(sagaOrchestrator).executeOrderSaga(request, 1L);
    }
    
    @Test
    void processOrder_InvalidRequest() {
        OrderRequest request = new OrderRequest("prod1", 2, BigDecimal.TEN);
        
        assertThrows(IllegalArgumentException.class, () -> orderService.processOrder(request, 1L));
    }
}
