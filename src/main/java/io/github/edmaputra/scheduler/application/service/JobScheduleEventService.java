package io.github.edmaputra.scheduler.application.service;

import io.github.edmaputra.scheduler.application.port.in.JobManagementUseCase;
import io.github.edmaputra.scheduler.application.port.in.JobScheduleEventUseCase;
import io.github.edmaputra.scheduler.application.port.out.InboundEventStorePort;
import io.github.edmaputra.scheduler.domain.JobType;
import io.github.edmaputra.scheduler.dto.CreateCronJobRequest;
import io.github.edmaputra.scheduler.dto.CreateDelayedJobRequest;
import io.github.edmaputra.scheduler.dto.JobResponse;
import io.github.edmaputra.scheduler.dto.JobScheduleEvent;
import io.github.edmaputra.scheduler.dto.MessageProcessingResult;
import io.github.edmaputra.scheduler.dto.MessageProcessingStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobScheduleEventService implements JobScheduleEventUseCase {

  private final JobManagementUseCase jobManagementUseCase;
  private final InboundEventStorePort inboundEventStorePort;

  @Override
  @Transactional
  public MessageProcessingResult handle(JobScheduleEvent event) {
    if (event == null) {
      throw new IllegalArgumentException("Job schedule event cannot be null");
    }
    if (event.getEventId() == null || event.getEventId().isBlank()) {
      throw new IllegalArgumentException("Event ID is required");
    }
    if (event.getJobType() == null) {
      throw new IllegalArgumentException("Job type is required");
    }

    if (!inboundEventStorePort.registerProcessing(event.getEventId(), event.getCorrelationId())) {
      return MessageProcessingResult.builder()
          .eventId(event.getEventId())
          .correlationId(event.getCorrelationId())
          .status(MessageProcessingStatus.DUPLICATE)
          .details("Duplicate event ignored")
          .build();
    }

    log.info("Handling job schedule event for job '{}' of type {}", event.getName(),
        event.getJobType());

    try {
      JobResponse response;

      if (event.getJobType() == JobType.CRON) {
        CreateCronJobRequest request = CreateCronJobRequest.builder()
            .name(event.getName())
            .description(event.getDescription())
            .cronExpression(event.getCronExpression())
            .payload(event.getPayload())
            .topic(event.getTopic())
            .createdBy(event.getCreatedBy())
            .build();
        response = jobManagementUseCase.createCronJob(request);
      } else if (event.getJobType() == JobType.DELAYED) {
        CreateDelayedJobRequest request = CreateDelayedJobRequest.builder()
            .name(event.getName())
            .description(event.getDescription())
            .scheduledTime(event.getScheduledTime())
            .payload(event.getPayload())
            .topic(event.getTopic())
            .createdBy(event.getCreatedBy())
            .build();
        response = jobManagementUseCase.createDelayedJob(request);
      } else {
        throw new IllegalArgumentException("Unsupported job type: " + event.getJobType());
      }

      inboundEventStorePort.markProcessed(event.getEventId(), response.getId());
      return MessageProcessingResult.builder()
          .eventId(event.getEventId())
          .correlationId(event.getCorrelationId())
          .jobId(response.getId())
          .status(MessageProcessingStatus.PROCESSED)
          .details("Job created successfully")
          .build();
    } catch (IllegalArgumentException ex) {
      inboundEventStorePort.markFailed(event.getEventId(), ex.getMessage());
      return MessageProcessingResult.builder()
          .eventId(event.getEventId())
          .correlationId(event.getCorrelationId())
          .status(MessageProcessingStatus.FAILED_PERMANENT)
          .details(ex.getMessage())
          .build();
    } catch (Exception ex) {
      inboundEventStorePort.markFailed(event.getEventId(), ex.getMessage());
      return MessageProcessingResult.builder()
          .eventId(event.getEventId())
          .correlationId(event.getCorrelationId())
          .status(MessageProcessingStatus.FAILED_RETRYABLE)
          .details(ex.getMessage())
          .build();
    }
  }
}
