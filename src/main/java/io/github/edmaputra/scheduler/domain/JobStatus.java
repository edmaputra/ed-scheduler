package io.github.edmaputra.scheduler.domain;

/**
 * Enum representing the current status of a job
 */
public enum JobStatus {
  /**
   * Job is scheduled and waiting to be executed
   */
  SCHEDULED,

  /**
   * Job is currently running
   */
  RUNNING,

  /**
   * Job completed successfully
   */
  COMPLETED,

  /**
   * Job failed during execution
   */
  FAILED,

  /**
   * Job was cancelled before completion
   */
  CANCELLED
}
