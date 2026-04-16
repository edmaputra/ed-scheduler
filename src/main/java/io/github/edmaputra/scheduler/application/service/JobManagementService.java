package io.github.edmaputra.scheduler.application.service;

import io.github.edmaputra.scheduler.application.port.in.JobManagementUseCase;
import io.github.edmaputra.scheduler.application.port.out.JobSchedulerPort;
import io.github.edmaputra.scheduler.application.port.out.JobStorePort;
import io.github.edmaputra.scheduler.domain.Job;
import io.github.edmaputra.scheduler.domain.JobStatus;
import io.github.edmaputra.scheduler.domain.JobType;
import io.github.edmaputra.scheduler.dto.CreateCronJobRequest;
import io.github.edmaputra.scheduler.dto.CreateDelayedJobRequest;
import io.github.edmaputra.scheduler.dto.JobResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobManagementService implements JobManagementUseCase {

  private final JobStorePort jobStorePort;
  private final JobSchedulerPort jobSchedulerPort;

  @Override
  @Transactional
  public JobResponse createCronJob(CreateCronJobRequest request) {
    log.info("Creating CRON job: {}", request.getName());

    if (!CronExpression.isValidExpression(request.getCronExpression())) {
      throw new IllegalArgumentException("Invalid CRON expression: " + request.getCronExpression());
    }

    Job job = Job.builder()
        .name(request.getName())
        .description(request.getDescription())
        .type(JobType.CRON)
        .status(JobStatus.PENDING)
        .cronExpression(request.getCronExpression())
        .payload(request.getPayload())
        .topic(request.getTopic())
        .createdBy(request.getCreatedBy())
        .build();

    job = jobStorePort.save(job);

    ScheduleHandle handle = jobSchedulerPort.scheduleCron(job);
    job.setQuartzJobKey(handle.jobKey());
    job.setQuartzTriggerKey(handle.triggerKey());
    job = jobStorePort.save(job);

    return mapToResponse(job);
  }

  @Override
  @Transactional
  public JobResponse createDelayedJob(CreateDelayedJobRequest request) {
    log.info("Creating delayed job: {}", request.getName());

    if (request.getScheduledTime() == null || request.getScheduledTime()
        .isBefore(LocalDateTime.now())) {
      throw new IllegalArgumentException("Scheduled time must be in the future");
    }

    Job job = Job.builder()
        .name(request.getName())
        .description(request.getDescription())
        .type(JobType.DELAYED)
        .status(JobStatus.PENDING)
        .scheduledTime(request.getScheduledTime())
        .payload(request.getPayload())
        .topic(request.getTopic())
        .createdBy(request.getCreatedBy())
        .build();

    job = jobStorePort.save(job);

    ScheduleHandle handle = jobSchedulerPort.scheduleDelayed(job);
    job.setQuartzJobKey(handle.jobKey());
    job.setQuartzTriggerKey(handle.triggerKey());
    job = jobStorePort.save(job);

    return mapToResponse(job);
  }

  @Override
  public JobResponse getJob(UUID jobId) {
    Job job = jobStorePort.findById(jobId)
        .orElseThrow(() -> new RuntimeException("Job not found: " + jobId));
    return mapToResponse(job);
  }

  @Override
  public List<JobResponse> getAllJobs() {
    return jobStorePort.findAll().stream()
        .map(this::mapToResponse)
        .toList();
  }

  @Override
  public List<JobResponse> getJobsByStatus(JobStatus status) {
    return jobStorePort.findByStatus(status).stream()
        .map(this::mapToResponse)
        .toList();
  }

  @Override
  public List<JobResponse> getJobsByType(JobType type) {
    return jobStorePort.findByType(type).stream()
        .map(this::mapToResponse)
        .toList();
  }

  @Override
  @Transactional
  public void cancelJob(UUID jobId) {
    Job job = jobStorePort.findById(jobId)
        .orElseThrow(() -> new RuntimeException("Job not found: " + jobId));

    if (job.getStatus().isTerminal()) {
      throw new IllegalStateException("Cannot cancel job in status: " + job.getStatus());
    }

    jobSchedulerPort.unschedule(job);
    job.setStatus(JobStatus.CANCELLED);
    jobStorePort.save(job);

    log.info("Job cancelled: {} (ID: {})", job.getName(), job.getId());
  }

  @Override
  @Transactional
  public void stopJob(UUID jobId) {
    Job job = jobStorePort.findById(jobId)
        .orElseThrow(() -> new RuntimeException("Job not found: " + jobId));

    if (job.getStatus().isTerminal()) {
      throw new IllegalStateException("Cannot stop job in status: " + job.getStatus());
    }

    jobSchedulerPort.interrupt(job);

    job.setStatus(JobStatus.STOPPED);
    job.setStoppedAt(LocalDateTime.now());
    jobStorePort.save(job);

    log.info("Job stopped: {} (ID: {})", job.getName(), job.getId());
  }

  private JobResponse mapToResponse(Job job) {
    return JobResponse.builder()
        .id(job.getId())
        .name(job.getName())
        .description(job.getDescription())
        .type(job.getType())
        .status(job.getStatus())
        .cronExpression(job.getCronExpression())
        .scheduledTime(job.getScheduledTime())
        .payload(job.getPayload())
        .topic(job.getTopic())
        .createdAt(job.getCreatedAt())
        .updatedAt(job.getUpdatedAt())
        .stoppedAt(job.getStoppedAt())
        .lastExecutionAt(job.getLastExecutionAt())
        .createdBy(job.getCreatedBy())
        .build();
  }
}
