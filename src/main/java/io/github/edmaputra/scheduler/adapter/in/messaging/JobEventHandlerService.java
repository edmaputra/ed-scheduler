package io.github.edmaputra.scheduler.adapter.in.messaging;

import io.github.edmaputra.scheduler.application.port.in.JobManagementUseCase;
import io.github.edmaputra.scheduler.dto.CreateCronJobRequest;
import io.github.edmaputra.scheduler.dto.CreateDelayedJobRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service that handles job scheduling events from message listeners
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JobEventHandlerService {

    private final JobManagementUseCase jobManagementUseCase;

    /**
     * Process job schedule event and create appropriate job
     * 
     * @param event The job schedule event
     */
    public void handleJobScheduleEvent(JobScheduleEvent event) {
        log.info("Handling job schedule event: {} (type: {})", event.getName(), event.getJobType());

        try {
            if ("CRON".equalsIgnoreCase(event.getJobType())) {
                CreateCronJobRequest request = CreateCronJobRequest.builder()
                        .name(event.getName())
                        .description(event.getDescription())
                        .cronExpression(event.getCronExpression())
                        .payload(event.getPayload())
                    .topic(event.getTopic())
                        .createdBy(event.getCreatedBy())
                        .build();

                jobManagementUseCase.createCronJob(request);
                log.info("CRON job created from message event: {}", event.getName());

            } else if ("DELAYED".equalsIgnoreCase(event.getJobType())) {
                CreateDelayedJobRequest request = CreateDelayedJobRequest.builder()
                        .name(event.getName())
                        .description(event.getDescription())
                        .scheduledTime(event.getScheduledTime())
                        .payload(event.getPayload())
                    .topic(event.getTopic())
                        .createdBy(event.getCreatedBy())
                        .build();

                jobManagementUseCase.createDelayedJob(request);
                log.info("Delayed job created from message event: {}", event.getName());

            } else {
                log.error("Unknown job type in event: {}", event.getJobType());
                throw new IllegalArgumentException("Unknown job type: " + event.getJobType());
            }

        } catch (Exception e) {
            log.error("Failed to handle job schedule event: {}", event.getName(), e);
            throw new RuntimeException("Failed to process job schedule event", e);
        }
    }
}
