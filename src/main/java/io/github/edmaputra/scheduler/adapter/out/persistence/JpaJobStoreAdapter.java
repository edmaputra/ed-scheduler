package io.github.edmaputra.scheduler.adapter.out.persistence;

import io.github.edmaputra.scheduler.adapter.out.persistence.repository.JobRepository;
import io.github.edmaputra.scheduler.application.port.out.JobStorePort;
import io.github.edmaputra.scheduler.domain.Job;
import io.github.edmaputra.scheduler.domain.JobStatus;
import io.github.edmaputra.scheduler.domain.JobType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaJobStoreAdapter implements JobStorePort {

  private final JobRepository jobRepository;

  @Override
  public Job save(Job job) {
    return jobRepository.save(job);
  }

  @Override
  public Optional<Job> findById(UUID id) {
    return jobRepository.findById(id);
  }

  @Override
  public List<Job> findAll() {
    return jobRepository.findAll();
  }

  @Override
  public List<Job> findByStatus(JobStatus status) {
    return jobRepository.findByStatus(status);
  }

  @Override
  public List<Job> findByType(JobType type) {
    return jobRepository.findByType(type);
  }
}
