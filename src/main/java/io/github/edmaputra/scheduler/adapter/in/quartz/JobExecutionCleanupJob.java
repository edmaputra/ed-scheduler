package io.github.edmaputra.scheduler.adapter.in.quartz;

import io.github.edmaputra.scheduler.application.port.out.JobExecutionStorePort;
import io.github.edmaputra.scheduler.domain.JobExecution;
import io.github.edmaputra.scheduler.domain.JobStatus;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Quartz job for cleaning up stale job executions
 * 
 * This job runs on a schedule and marks job executions that have been in RUNNING state
 * for longer than 1 hour as TIMEOUT. It runs on only one instance in a multi-instance
 * deployment due to Quartz clustering.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JobExecutionCleanupJob implements Job {

  private static final int STALE_EXECUTION_THRESHOLD_HOURS = 1;

  private final JobExecutionStorePort jobExecutionStorePort;

  @Override
  @Transactional
  public void execute(JobExecutionContext context) throws JobExecutionException {
    try {
      markStaleRunningExecutionsAsTimeout();
    } catch (Exception e) {
      log.error("Error executing job execution cleanup", e);
      throw new JobExecutionException(e);
    }
  }

  private void markStaleRunningExecutionsAsTimeout() {
    LocalDateTime cutoff = LocalDateTime.now().minusHours(STALE_EXECUTION_THRESHOLD_HOURS);
    List<JobExecution> staleExecutions = jobExecutionStorePort.findByStatusAndStartedAtBefore(
        JobStatus.RUNNING, cutoff);

    if (staleExecutions.isEmpty()) {
      log.debug("No stale job executions found");
      return;
    }

    for (JobExecution execution : staleExecutions) {
      execution.setStatus(JobStatus.TIMEOUT);
      execution.setCompletedAt(LocalDateTime.now());
      execution.setStaleSince(LocalDateTime.now());
      execution.setErrorMessage("Execution timed out after one hour without completing");
      jobExecutionStorePort.save(execution);

      log.warn("Marked stale job execution {} for job {} as TIMEOUT", execution.getId(),
          execution.getJob().getId());
    }

    log.info("Cleanup job completed: marked {} stale executions as TIMEOUT", staleExecutions.size());
  }
}
