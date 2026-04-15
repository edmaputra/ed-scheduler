package io.github.edmaputra.scheduler.adapter.out.persistence.repository;

import io.github.edmaputra.scheduler.domain.Job;
import io.github.edmaputra.scheduler.domain.JobExecution;
import io.github.edmaputra.scheduler.domain.JobStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for JobExecution entity
 */
@Repository
public interface JobExecutionRepository extends JpaRepository<JobExecution, UUID> {

  /**
   * Find all executions for a specific job
   */
  List<JobExecution> findByJobOrderByStartedAtDesc(Job job);

  /**
   * Find executions by status
   */
  List<JobExecution> findByStatus(JobStatus status);

  /**
   * Find latest execution for a job
   */
  JobExecution findFirstByJobOrderByStartedAtDesc(Job job);
}
