package com.example.mircoservices.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.mircoservices.order.model.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
