package com.example.microservices.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.microservices.order.model.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
