package com.example.microservices.order.rest.api;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {
    private String orderNumber;
    private String skuCode;
    private BigDecimal price;
    private Integer quantity;
    private UserDetails userDetails;
}
