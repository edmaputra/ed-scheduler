package io.github.edmaputra.scheduler.application.port.out;

import java.util.UUID;

public interface InboundEventStorePort {

  boolean registerProcessing(String eventId, String correlationId);

  void markProcessed(String eventId, UUID jobId);

  void markFailed(String eventId, String errorMessage);
}
