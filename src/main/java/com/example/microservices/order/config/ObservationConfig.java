package com.example.microservices.order.config;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.aop.ObservedAspect;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

import com.example.microservices.order.event.OrderPlacedEvent;

@Configuration
@RequiredArgsConstructor
public class ObservationConfig {

    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    @PostConstruct
    public void setObservationForKafkaTemplate() {
        kafkaTemplate.setObservationEnabled(true);
    }

    @Bean
    ObservedAspect observedAspect(ObservationRegistry registry) {
        return new ObservedAspect(registry);
    }
}
