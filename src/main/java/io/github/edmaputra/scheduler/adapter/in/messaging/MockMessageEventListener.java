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
@ConditionalOnProperty(name = "scheduler.messaging.inbound.provider", havingValue = "mock")
public class MockMessageEventListener implements MessageEventListener {

  private final JobScheduleEventUseCase jobScheduleEventUseCase;

  @Override
  public MessageProcessingResult onJobScheduleEvent(JobScheduleEvent event) {
    log.info("Mock inbound adapter received job schedule event for '{}'", event.getName());
    return jobScheduleEventUseCase.handle(event);
  }
}
