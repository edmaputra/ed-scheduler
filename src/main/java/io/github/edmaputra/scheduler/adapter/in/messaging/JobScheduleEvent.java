package io.github.edmaputra.scheduler.adapter.in.messaging;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event model for job scheduling via message
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobScheduleEvent {

  /**
   * Type of job: CRON or DELAYED
   */
  private String jobType;

  /**
   * Job name
   */
  private String name;

  /**
   * Job description
   */
  private String description;

  /**
   * CRON expression (for CRON jobs)
   */
  private String cronExpression;

  /**
   * Scheduled time (for DELAYED jobs)
   */
  private LocalDateTime scheduledTime;

  /**
   * Job payload (JSON string)
   */
  private String payload;

  /**
   * Topic/queue destination used when the job executes
   */
  private String topic;

  /**
   * User who created the job
   */
  private String createdBy;
}
