package io.github.edmaputra.scheduler.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a single execution of a job
 */
@Entity
@Table(name = "job_executions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatus status;

    /**
     * Error message if execution failed
     */
    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * Stack trace if execution failed
     */
    @Column(name = "error_stack_trace", columnDefinition = "TEXT")
    private String errorStackTrace;

    /**
     * Result data from job execution (if any)
     */
    @Column(columnDefinition = "TEXT")
    private String result;

    @PrePersist
    protected void onCreate() {
        if (startedAt == null) {
            startedAt = LocalDateTime.now();
        }
        if (status == null) {
            status = JobStatus.RUNNING;
        }
    }
}
