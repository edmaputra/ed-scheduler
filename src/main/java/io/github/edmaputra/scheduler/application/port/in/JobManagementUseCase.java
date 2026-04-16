package io.github.edmaputra.scheduler.application.port.in;

import io.github.edmaputra.scheduler.domain.JobStatus;
import io.github.edmaputra.scheduler.domain.JobType;
import io.github.edmaputra.scheduler.dto.CreateCronJobRequest;
import io.github.edmaputra.scheduler.dto.CreateDelayedJobRequest;
import io.github.edmaputra.scheduler.dto.JobResponse;

import java.util.List;
import java.util.UUID;

public interface JobManagementUseCase {

  JobResponse createCronJob(CreateCronJobRequest request);

  JobResponse createDelayedJob(CreateDelayedJobRequest request);

  JobResponse getJob(UUID jobId);

  List<JobResponse> getAllJobs();

  List<JobResponse> getJobsByStatus(JobStatus status);

  List<JobResponse> getJobsByType(JobType type);

  void cancelJob(UUID jobId);

  void stopJob(UUID jobId);
}
