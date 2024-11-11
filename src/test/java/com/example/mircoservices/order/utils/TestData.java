package com.example.mircoservices.order.utils;

import java.math.BigDecimal;

import com.example.microservices.order.rest.api.OrderRequest;
import com.example.microservices.order.rest.api.UserDetails;

public class TestData {

    public static OrderRequest buildOrderRequest(Long id) {
        return OrderRequest.builder()
                .skuCode("sku")
                .price(new BigDecimal(12))
                .orderNumber("orderNum")
                .userDetails(UserDetails.builder()
                        .firstName("Joe")
                        .lastName("Blogs")
                        .email("joe.b@fred.com")
                        .build())
                .build();
    }
}
