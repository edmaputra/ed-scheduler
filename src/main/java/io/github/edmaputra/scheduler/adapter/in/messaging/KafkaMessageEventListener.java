package io.github.edmaputra.scheduler.adapter.in.messaging;

import io.github.edmaputra.scheduler.application.port.in.JobScheduleEventUseCase;
import io.github.edmaputra.scheduler.dto.JobScheduleEvent;
import io.github.edmaputra.scheduler.dto.MessageProcessingResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "scheduler.messaging.inbound.provider", havingValue = "kafka")
public class KafkaMessageEventListener implements MessageEventListener {

  private final JobScheduleEventUseCase jobScheduleEventUseCase;

  @Override
  public MessageProcessingResult onJobScheduleEvent(JobScheduleEvent event) {
    // This method is intentionally provider-agnostic. A future Kafka listener
    // should deserialize records and forward them to this method.
    log.info("Kafka inbound adapter received job schedule event for '{}'", event.getName());
    return jobScheduleEventUseCase.handle(event);
  }
}
