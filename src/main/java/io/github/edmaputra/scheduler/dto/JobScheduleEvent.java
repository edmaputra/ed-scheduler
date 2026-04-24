package io.github.edmaputra.scheduler.dto;

import io.github.edmaputra.scheduler.domain.JobType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Message payload used by inbound message adapters to request job creation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobScheduleEvent {

  private String eventId;
  private String correlationId;
  private JobType jobType;
  private String name;
  private String description;
  private String cronExpression;
  private LocalDateTime scheduledTime;
  private String payload;
  private String topic;
  private String createdBy;
}
