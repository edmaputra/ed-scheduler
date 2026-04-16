package io.github.edmaputra.scheduler.adapter.in.web;

import io.github.edmaputra.scheduler.application.port.in.JobManagementUseCase;
import io.github.edmaputra.scheduler.domain.JobStatus;
import io.github.edmaputra.scheduler.domain.JobType;
import io.github.edmaputra.scheduler.dto.CreateCronJobRequest;
import io.github.edmaputra.scheduler.dto.CreateDelayedJobRequest;
import io.github.edmaputra.scheduler.dto.JobResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for job management
 */
@Slf4j
@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

  private final JobManagementUseCase jobManagementUseCase;

  /**
   * Create a new CRON job
   */
  @PostMapping("/cron")
  public ResponseEntity<JobResponse> createCronJob(@RequestBody CreateCronJobRequest request) {
    log.info("Received request to create CRON job: {}", request.getName());
    JobResponse response = jobManagementUseCase.createCronJob(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * Create a new delayed job
   */
  @PostMapping("/delayed")
  public ResponseEntity<JobResponse> createDelayedJob(
      @RequestBody CreateDelayedJobRequest request) {
    log.info("Received request to create delayed job: {}", request.getName());
    JobResponse response = jobManagementUseCase.createDelayedJob(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * Get job by ID
   */
  @GetMapping("/{id}")
  public ResponseEntity<JobResponse> getJob(@PathVariable UUID id) {
    log.info("Received request to get job: {}", id);
    JobResponse response = jobManagementUseCase.getJob(id);
    return ResponseEntity.ok(response);
  }

  /**
   * Get all jobs with optional filters
   */
  @GetMapping
  public ResponseEntity<List<JobResponse>> getAllJobs(
      @RequestParam(required = false) JobStatus status,
      @RequestParam(required = false) JobType type) {
    log.info("Received request to get all jobs (status: {}, type: {})", status, type);

    List<JobResponse> response;
    if (status != null) {
      response = jobManagementUseCase.getJobsByStatus(status);
    } else if (type != null) {
      response = jobManagementUseCase.getJobsByType(type);
    } else {
      response = jobManagementUseCase.getAllJobs();
    }

    return ResponseEntity.ok(response);
  }

  /**
   * Cancel a job
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> cancelJob(@PathVariable UUID id) {
    log.info("Received request to cancel job: {}", id);
    jobManagementUseCase.cancelJob(id);
    return ResponseEntity.noContent().build();
  }

  /**
   * Stop a running job
   */
  @PostMapping("/{id}/stop")
  public ResponseEntity<Void> stopJob(@PathVariable UUID id) {
    log.info("Received request to stop job: {}", id);
    jobManagementUseCase.stopJob(id);
    return ResponseEntity.noContent().build();
  }
}
