package io.github.edmaputra.scheduler.application.port.out;

import io.github.edmaputra.scheduler.domain.Job;
import io.github.edmaputra.scheduler.domain.JobStatus;
import io.github.edmaputra.scheduler.domain.JobType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JobStorePort {

  Job save(Job job);

  Optional<Job> findById(UUID id);

  List<Job> findAll();

  List<Job> findByStatus(JobStatus status);

  List<Job> findByType(JobType type);
}
