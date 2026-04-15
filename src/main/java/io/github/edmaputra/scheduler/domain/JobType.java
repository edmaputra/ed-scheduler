package io.github.edmaputra.scheduler.domain;

/**
 * Enum representing the type of job to be scheduled
 */
public enum JobType {
  /**
   * Job that runs on a CRON schedule (recurring)
   */
  CRON,

  /**
   * Job that runs once at a specific time (delayed execution)
   */
  DELAYED
}
