package io.github.edmaputra.scheduler.dto;

import io.github.edmaputra.scheduler.domain.JobStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for job execution information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobExecutionResponse {

    private UUID id;
    private UUID jobId;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private JobStatus status;
    private String errorMessage;
    private String result;
}
