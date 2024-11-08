package com.example.microservices.order.service;

import java.text.MessageFormat;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.microservices.order.client.InventoryClient;
import com.example.microservices.order.client.NotificationClient;
import com.example.microservices.order.event.OrderPlacedEvent;
import com.example.microservices.order.model.Order;
import com.example.microservices.order.repository.OrderRepository;
import com.example.microservices.order.rest.api.OrderRequest;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;
    private final NotificationClient notificationClient;

    public void placeOrder(OrderRequest orderRequest) {
        boolean inStock = inventoryClient.isInStock(orderRequest.getSkuCode(), orderRequest.getQuantity());
        if (inStock) {
            var order = mapToOrder(orderRequest);
            Order savedOrder = orderRepository.save(order);
            // Send notification email
            OrderPlacedEvent orderPlacedEvent = new OrderPlacedEvent(order.getOrderNumber(),
                    orderRequest.getUserDetails().getEmail(),
                    orderRequest.getUserDetails().getFirstName(),
                    orderRequest.getUserDetails().getLastName());
            log.info("Start - Sending OrderPlacedEvent {} to Kafka topic order-placed", orderPlacedEvent);
            notificationClient.send(Long.toString(savedOrder.getId()), orderPlacedEvent);
            log.info("End - Sending OrderPlacedEvent {} to Kafka topic order-placed", orderPlacedEvent);

        } else {
            throw new RuntimeException(
                    MessageFormat.format("Product with Skucode {0} is not in stock", orderRequest.getSkuCode()));
        }
    }

    private static Order mapToOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        order.setPrice(orderRequest.getPrice());
        order.setQuantity(orderRequest.getQuantity());
        order.setSkuCode(orderRequest.getSkuCode());
        return order;
    }
}
