package io.github.edmaputra.scheduler.application.port.in;

import java.util.UUID;

public interface ScheduledJobExecutionUseCase {

  void execute(UUID jobId);
}
