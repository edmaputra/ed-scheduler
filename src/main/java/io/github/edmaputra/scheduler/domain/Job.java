package io.github.edmaputra.scheduler.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a scheduled job
 */
@Entity
@Table(name = "jobs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatus status;

    /**
     * CRON expression for CRON jobs (e.g., "0 0 12 * * ?")
     * Null for DELAYED jobs
     */
    @Column(name = "cron_expression")
    private String cronExpression;

    /**
     * Execution time for DELAYED jobs
     * Null for CRON jobs
     */
    @Column(name = "scheduled_time")
    private LocalDateTime scheduledTime;

    /**
     * JSON payload containing job-specific data
     */
    @Column(columnDefinition = "TEXT")
    private String payload;

    /**
     * Topic/queue name to publish message to when job executes
     * If null, no message will be published
     */
    @Column(name = "topic")
    private String topic;

    /**
     * Quartz job key for tracking in Quartz scheduler
     */
    @Column(name = "quartz_job_key")
    private String quartzJobKey;

    /**
     * Quartz trigger key for tracking in Quartz scheduler
     */
    @Column(name = "quartz_trigger_key")
    private String quartzTriggerKey;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = JobStatus.SCHEDULED;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
