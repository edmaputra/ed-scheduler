package io.github.edmaputra.scheduler.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Request DTO for creating a new delayed job
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateDelayedJobRequest {

    private String name;
    private String description;
    private LocalDateTime scheduledTime;
    private String payload;
    private String topic;
    private String createdBy;
}
