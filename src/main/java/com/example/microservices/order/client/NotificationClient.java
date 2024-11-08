package com.example.microservices.order.client;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import com.example.microservices.order.event.OrderPlacedEvent;
import com.example.microservices.order.properties.OrderApplicationProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationClient {
    @Autowired
    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    @Autowired
    private final OrderApplicationProperties topicConfig;

    public SendResult<String, OrderPlacedEvent> send(String key, OrderPlacedEvent orderPlacedEvent) {

        try {
            final ProducerRecord<String, OrderPlacedEvent> record = new ProducerRecord<>(
                    topicConfig.getOrderPlacedTopic(),
                    key,
                    orderPlacedEvent);
            final SendResult<String, OrderPlacedEvent> result = kafkaTemplate.send(record).get();
            final RecordMetadata metadata = result.getRecordMetadata();

            log.debug(String.format("Sent order(order number=%s email=%s) meta(topic=%s, partition=%d, offset=%d)",
                    orderPlacedEvent.getOrderNumber(), orderPlacedEvent.getEmail(), metadata.topic(),
                    metadata.partition(), metadata.offset()));
            return result;
        } catch (Exception e) {
            String message = "Error sending message to topic: " + topicConfig.getOrderPlacedTopic() + " "
                    + e.getCause();
            log.error(message);
            throw new RuntimeException(message, e);
        }
    }
}
