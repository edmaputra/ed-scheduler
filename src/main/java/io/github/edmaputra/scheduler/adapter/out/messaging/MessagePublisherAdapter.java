package io.github.edmaputra.scheduler.adapter.out.messaging;

import io.github.edmaputra.scheduler.application.port.out.MessageDispatchPort;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessagePublisherAdapter implements MessageDispatchPort {

  private final MessagePublisher messagePublisher;

  @Override
  public void publish(String topic, String payload, Map<String, String> headers) {
    messagePublisher.publish(topic, payload, headers);
  }
}
