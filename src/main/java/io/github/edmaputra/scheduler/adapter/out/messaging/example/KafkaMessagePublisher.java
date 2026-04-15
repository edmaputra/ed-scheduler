package io.github.edmaputra.scheduler.adapter.out.messaging.example;

import io.github.edmaputra.scheduler.adapter.out.messaging.MessagePublisher;
import lombok.extern.slf4j.Slf4j;
// import org.springframework.kafka.core.KafkaTemplate;
// import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Example Kafka implementation of MessagePublisher
 * 
 * To use this:
 * 1. Add Kafka dependency to build.gradle:
 * implementation 'org.springframework.kafka:spring-kafka'
 * 
 * 2. Configure Kafka in application.properties:
 * spring.kafka.bootstrap-servers=localhost:9092
 * spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
 * spring.kafka.producer.value-serializer=org.apache.kafka.common.serialization.StringSerializer
 * 
 * 3. Uncomment the @Component annotation and KafkaTemplate import
 */
@Slf4j
// @Component // Uncomment to enable
public class KafkaMessagePublisher implements MessagePublisher {

    // @Autowired
    // private KafkaTemplate<String, String> kafkaTemplate;

    @Override
    public void publish(String topic, String payload) {
        log.info("Publishing message to Kafka topic: {}", topic);
        // kafkaTemplate.send(topic, payload);
        log.info("Message published successfully to topic: {}", topic);
    }

    @Override
    public void publish(String topic, String payload, Map<String, String> headers) {
        log.info("Publishing message to Kafka topic: {} with headers", topic);

        // ProducerRecord<String, String> record = new ProducerRecord<>(topic, payload);
        // headers.forEach((key, value) -> record.headers().add(key, value.getBytes()));
        // kafkaTemplate.send(record);

        log.info("Message published successfully to topic: {} with headers: {}", topic, headers);
    }
}
