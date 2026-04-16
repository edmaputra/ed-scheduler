package io.github.edmaputra.scheduler.application.service;

import io.github.edmaputra.scheduler.application.port.out.JobExecutionStorePort;
import io.github.edmaputra.scheduler.domain.JobExecution;
import io.github.edmaputra.scheduler.domain.JobStatus;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobExecutionCleanupService {

  private static final int STALE_EXECUTION_THRESHOLD_HOURS = 1;

  private final JobExecutionStorePort jobExecutionStorePort;

  @Scheduled(fixedDelay = 300000)
  @Transactional
  public void markStaleRunningExecutionsAsTimeout() {
    LocalDateTime cutoff = LocalDateTime.now().minusHours(STALE_EXECUTION_THRESHOLD_HOURS);
    List<JobExecution> staleExecutions = jobExecutionStorePort.findByStatusAndStartedAtBefore(
        JobStatus.RUNNING, cutoff);

    if (staleExecutions.isEmpty()) {
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
  }
}
