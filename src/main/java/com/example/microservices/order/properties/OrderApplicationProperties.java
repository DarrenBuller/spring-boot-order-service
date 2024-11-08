package com.example.microservices.order.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotNull;

@Configuration
@ConfigurationProperties(prefix = "order-application", ignoreUnknownFields = false)
@Getter
@Setter
@Validated
public class OrderApplicationProperties {
    @NotNull
    private String orderPlacedTopic;

    @NotNull
    private String inventoryUrl;
}
