package io.github.edmaputra.scheduler.application.port.in;

import io.github.edmaputra.scheduler.dto.JobScheduleEvent;
import io.github.edmaputra.scheduler.dto.MessageProcessingResult;

public interface JobScheduleEventUseCase {

  MessageProcessingResult handle(JobScheduleEvent event);
}
