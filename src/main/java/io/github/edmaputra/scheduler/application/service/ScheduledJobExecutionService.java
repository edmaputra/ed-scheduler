package io.github.edmaputra.scheduler.application.service;

import io.github.edmaputra.scheduler.application.port.in.ScheduledJobExecutionUseCase;
import io.github.edmaputra.scheduler.application.port.out.JobExecutionStorePort;
import io.github.edmaputra.scheduler.application.port.out.JobStorePort;
import io.github.edmaputra.scheduler.application.port.out.MessageDispatchPort;
import io.github.edmaputra.scheduler.domain.Job;
import io.github.edmaputra.scheduler.domain.JobExecution;
import io.github.edmaputra.scheduler.domain.JobStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduledJobExecutionService implements ScheduledJobExecutionUseCase {

    private final JobStorePort jobStorePort;
    private final JobExecutionStorePort jobExecutionStorePort;
    private final MessageDispatchPort messageDispatchPort;

    @Override
    @Transactional
    public void execute(UUID jobId) {
        log.info("Executing job with ID: {}", jobId);

        Job job = jobStorePort.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found: " + jobId));

        JobExecution execution = JobExecution.builder()
                .job(job)
                .startedAt(LocalDateTime.now())
                .status(JobStatus.RUNNING)
                .build();
        execution = jobExecutionStorePort.save(execution);

        job.setStatus(JobStatus.RUNNING);
        jobStorePort.save(job);

        try {
            String result = executeJobLogic(job);

            execution.setCompletedAt(LocalDateTime.now());
            execution.setStatus(JobStatus.COMPLETED);
            execution.setResult(result);
            jobExecutionStorePort.save(execution);

            job.setStatus(JobStatus.COMPLETED);
            jobStorePort.save(job);

            log.info("Job {} completed successfully", jobId);

        } catch (Exception e) {
            log.error("Job {} failed with error: {}", jobId, e.getMessage(), e);

            execution.setCompletedAt(LocalDateTime.now());
            execution.setStatus(JobStatus.FAILED);
            execution.setErrorMessage(e.getMessage());
            execution.setErrorStackTrace(getStackTraceAsString(e));
            jobExecutionStorePort.save(execution);

            job.setStatus(JobStatus.FAILED);
            jobStorePort.save(job);

            throw new RuntimeException("Job execution failed", e);
        }
    }

    private String executeJobLogic(Job job) {
        log.info("Executing job: {} (Type: {}, Topic: {})", job.getName(), job.getType(), job.getTopic());

        if (job.getTopic() == null || job.getTopic().isEmpty()) {
            log.warn("No topic configured for job: {}", job.getName());
            return "Job executed but no topic configured for message publishing";
        }

        Map<String, String> headers = new HashMap<>();
        headers.put("job-id", job.getId().toString());
        headers.put("job-name", job.getName());
        headers.put("job-type", job.getType().toString());
        headers.put("execution-time", LocalDateTime.now().toString());

        messageDispatchPort.publish(job.getTopic(), job.getPayload(), headers);

        log.info("Published message to topic: {}", job.getTopic());
        return "Message published to topic: " + job.getTopic();
    }

    private String getStackTraceAsString(Exception e) {
        StringBuilder sb = new StringBuilder();
        sb.append(e).append("\n");
        for (StackTraceElement element : e.getStackTrace()) {
            sb.append("\tat ").append(element).append("\n");
        }
        return sb.toString();
    }
}
