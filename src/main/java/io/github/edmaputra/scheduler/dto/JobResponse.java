package io.github.edmaputra.scheduler.dto;

import io.github.edmaputra.scheduler.domain.JobStatus;
import io.github.edmaputra.scheduler.domain.JobType;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for job information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobResponse {

  private UUID id;
  private String name;
  private String description;
  private JobType type;
  private JobStatus status;
  private String cronExpression;
  private LocalDateTime scheduledTime;
  private String payload;
  private String topic;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private String createdBy;
}
