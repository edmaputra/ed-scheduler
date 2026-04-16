package io.github.edmaputra.scheduler.application.port.out;

import io.github.edmaputra.scheduler.domain.JobExecution;
import io.github.edmaputra.scheduler.domain.JobStatus;
import java.time.LocalDateTime;
import java.util.List;

public interface JobExecutionStorePort {

  JobExecution save(JobExecution execution);

  List<JobExecution> findByStatusAndStartedAtBefore(JobStatus status, LocalDateTime startedAt);
}
