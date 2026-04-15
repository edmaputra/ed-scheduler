package io.github.edmaputra.scheduler.application.port.out;

import io.github.edmaputra.scheduler.domain.JobExecution;

public interface JobExecutionStorePort {

    JobExecution save(JobExecution execution);
}
