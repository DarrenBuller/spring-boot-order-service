package com.example.mircoservices.order;

import org.springframework.boot.SpringApplication;

import com.example.microservices.order.OrderApplication;

public class TestOrderApplication {

	public static void main(String[] args) {
		SpringApplication.from(OrderApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
