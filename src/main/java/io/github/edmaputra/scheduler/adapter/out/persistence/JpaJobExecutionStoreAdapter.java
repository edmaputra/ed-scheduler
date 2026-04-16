package io.github.edmaputra.scheduler.adapter.out.persistence;

import io.github.edmaputra.scheduler.adapter.out.persistence.repository.JobExecutionRepository;
import io.github.edmaputra.scheduler.application.port.out.JobExecutionStorePort;
import io.github.edmaputra.scheduler.domain.JobExecution;
import io.github.edmaputra.scheduler.domain.JobStatus;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaJobExecutionStoreAdapter implements JobExecutionStorePort {

  private final JobExecutionRepository jobExecutionRepository;

  @Override
  public JobExecution save(JobExecution execution) {
    return jobExecutionRepository.save(execution);
  }

  @Override
  public List<JobExecution> findByStatusAndStartedAtBefore(JobStatus status,
      LocalDateTime startedAt) {
    return jobExecutionRepository.findByStatusAndStartedAtBefore(status, startedAt);
  }
}
