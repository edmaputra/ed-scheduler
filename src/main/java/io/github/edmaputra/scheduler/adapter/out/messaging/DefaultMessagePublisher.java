package io.github.edmaputra.scheduler.adapter.out.messaging;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * Default no-op implementation of MessagePublisher This is used when no actual message publisher is
 * configured
 * <p>
 * To use a real message publisher (Kafka, RabbitMQ, etc.), create a @Component that implements
 * MessagePublisher interface and this default will be disabled.
 */
@Slf4j
public class DefaultMessagePublisher implements MessagePublisher {

  @Override
  public void publish(String topic, String payload) {
    log.info("DefaultMessagePublisher: Would publish to topic '{}' with payload: {}", topic,
        payload);
    log.warn(
        "No real MessagePublisher configured. Implement MessagePublisher interface to enable actual message publishing.");
  }

  @Override
  public void publish(String topic, String payload, Map<String, String> headers) {
    log.info(
        "DefaultMessagePublisher: Would publish to topic '{}' with payload: {} and headers: {}",
        topic, payload, headers);
    log.warn(
        "No real MessagePublisher configured. Implement MessagePublisher interface to enable actual message publishing.");
  }
}
