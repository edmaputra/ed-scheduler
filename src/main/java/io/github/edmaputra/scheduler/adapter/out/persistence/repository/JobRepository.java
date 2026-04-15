package io.github.edmaputra.scheduler.adapter.out.persistence.repository;

import io.github.edmaputra.scheduler.domain.Job;
import io.github.edmaputra.scheduler.domain.JobStatus;
import io.github.edmaputra.scheduler.domain.JobType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for Job entity
 */
@Repository
public interface JobRepository extends JpaRepository<Job, UUID> {

  /**
   * Find jobs by status
   */
  List<Job> findByStatus(JobStatus status);

  /**
   * Find jobs by type
   */
  List<Job> findByType(JobType type);

  /**
   * Find job by Quartz job key
   */
  Optional<Job> findByQuartzJobKey(String quartzJobKey);

  /**
   * Find jobs by status and type
   */
  List<Job> findByStatusAndType(JobStatus status, JobType type);
}
