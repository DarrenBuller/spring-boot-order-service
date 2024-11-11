package com.example.mircoservices.order.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.example.microservices.order.controller.OrderController;
import com.example.microservices.order.rest.api.OrderRequest;
import com.example.microservices.order.service.OrderService;
import com.example.mircoservices.order.utils.TestData;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class OrderControllerTest {

    private OrderService serviceMock;
    private OrderController controller;

    @BeforeEach
    public void setUp() {
        serviceMock = mock(OrderService.class);
        controller = new OrderController(serviceMock);
    }

    /**
     * Ensure that the REST request is successfully passed on to the service.
     */
    @Test
    public void statusCreatedWhenOrderSuccess() {
        OrderRequest request = TestData.buildOrderRequest(1L);
        ResponseEntity<String> response = controller.placeOrder(request);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.CREATED));
        assertThat(response.getBody(), equalTo("Order Placed Successfully"));
        verify(serviceMock, times(1)).placeOrder(request);
    }

    /**
     * If an exception is thrown, an error is logged and internal server error is
     * returned
     */
    @Test
    public void statusInternalServiceErrorWhenOrderServiceRaisesException() {
        OrderRequest request = TestData.buildOrderRequest(1L);
        doThrow(new RuntimeException("Service failure")).when(serviceMock).placeOrder(request);
        ResponseEntity<String> response = controller.placeOrder(request);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.INTERNAL_SERVER_ERROR));
        assertThat(response.getBody(), equalTo("Failed to place order"));
        verify(serviceMock, times(1)).placeOrder(request);
    }
}
