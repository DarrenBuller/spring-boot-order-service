package com.example.mircoservices.order.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.example.microservices.order.client.InventoryClient;
import com.example.microservices.order.client.NotificationClient;
import com.example.microservices.order.event.OrderPlacedEvent;
import com.example.microservices.order.repository.OrderRepository;
import com.example.microservices.order.rest.api.OrderRequest;
import com.example.microservices.order.service.OrderService;
import com.example.mircoservices.order.utils.TestData;
import com.example.microservices.order.model.Order;

public class OrderServiceTest {
    private OrderRepository mockOrderRepository;
    private InventoryClient mockInventoryClient;
    private NotificationClient mockNotificationClient;
    private OrderService orderService;

    @BeforeEach
    public void setUp() {
        mockOrderRepository = mock(OrderRepository.class);
        mockNotificationClient = mock(NotificationClient.class);
        mockInventoryClient = mock(InventoryClient.class);

        orderService = new OrderService(mockOrderRepository, mockInventoryClient, mockNotificationClient);
    }

    @Test
    public void testInStockNotificationSucess() {
        Long key = 1L;
        OrderRequest orderRequest = TestData.buildOrderRequest(key);
        when(mockInventoryClient.isInStock(orderRequest.getSkuCode(), orderRequest.getQuantity())).thenReturn(true);
        Order newOrder = new Order(1L, "orderNumber", "skuCode", new BigDecimal(1), 100);
        when(mockOrderRepository.save(any(Order.class))).thenReturn(newOrder);

        orderService.placeOrder(orderRequest);
        verify(mockNotificationClient, times(1)).send(eq(Long.toString(key)), any(OrderPlacedEvent.class));
    }

    @Test
    public void testOutOfStockRaisesException() {
        Long key = 1L;
        OrderRequest orderRequest = TestData.buildOrderRequest(key);
        when(mockInventoryClient.isInStock(orderRequest.getSkuCode(), orderRequest.getQuantity())).thenReturn(false);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            orderService.placeOrder(orderRequest);
        });
        assertTrue(exception.getMessage().contains("not in stock"));
    }

}
