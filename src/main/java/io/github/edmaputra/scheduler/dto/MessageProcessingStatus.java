package io.github.edmaputra.scheduler.dto;

public enum MessageProcessingStatus {
  PROCESSED,
  DUPLICATE,
  FAILED_PERMANENT,
  FAILED_RETRYABLE
}
