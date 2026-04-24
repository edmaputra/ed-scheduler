package io.github.edmaputra.scheduler.adapter.in.messaging;

import io.github.edmaputra.scheduler.dto.JobScheduleEvent;
import io.github.edmaputra.scheduler.dto.MessageProcessingResult;

/**
 * Shared adapter-in contract for inbound message consumers.
 */
public interface MessageEventListener {

  MessageProcessingResult onJobScheduleEvent(JobScheduleEvent event);
}
