package io.github.edmaputra.scheduler.domain;

/**
 * Enum representing the current status of a job or a job execution.
 */
public enum JobStatus {
  /**
   * Job is scheduled and waiting to be executed
   */
  SCHEDULED,

  /**
   * Job is created but not yet executed.
   */
  PENDING,

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
  CANCELLED,

  /**
   * Job was stopped by the user and unscheduled.
   */
  STOPPED,

  /**
   * Job execution was considered stale by the cleanup process.
   */
  TIMEOUT;

  /**
   * Checks if the current status is a terminal status.
   * Terminal statuses are: COMPLETED, CANCELLED, STOPPED, TIMEOUT, and INTERRUPTED.
   *
   * @return true if the status is terminal, false otherwise
   */
  public boolean isTerminal() {
    return this == COMPLETED || this == CANCELLED || this == STOPPED || this == TIMEOUT;
  }
}
