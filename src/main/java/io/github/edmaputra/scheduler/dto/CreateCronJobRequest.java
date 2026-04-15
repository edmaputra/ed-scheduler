package io.github.edmaputra.scheduler.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new CRON job
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCronJobRequest {

    private String name;
    private String description;
    private String cronExpression;
    private String payload;
    private String topic;
    private String createdBy;
}
