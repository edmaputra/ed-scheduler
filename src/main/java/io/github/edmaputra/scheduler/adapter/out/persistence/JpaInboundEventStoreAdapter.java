package io.github.edmaputra.scheduler.adapter.out.persistence;

import io.github.edmaputra.scheduler.adapter.out.persistence.repository.InboundMessageEventRepository;
import io.github.edmaputra.scheduler.application.port.out.InboundEventStorePort;
import io.github.edmaputra.scheduler.domain.InboundEventStatus;
import io.github.edmaputra.scheduler.domain.InboundMessageEvent;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JpaInboundEventStoreAdapter implements InboundEventStorePort {

  private final InboundMessageEventRepository inboundMessageEventRepository;

  @Override
  public boolean registerProcessing(String eventId, String correlationId) {
    if (inboundMessageEventRepository.existsById(eventId)) {
      log.info("Duplicate inbound message event ignored: {}", eventId);
      return false;
    }

    InboundMessageEvent entity = InboundMessageEvent.builder()
        .eventId(eventId)
        .correlationId(correlationId)
        .status(InboundEventStatus.PROCESSING)
        .build();
    inboundMessageEventRepository.save(entity);
    return true;
  }

  @Override
  public void markProcessed(String eventId, UUID jobId) {
    InboundMessageEvent entity = inboundMessageEventRepository.findById(eventId)
        .orElseThrow(() -> new IllegalStateException("Inbound event not found: " + eventId));
    entity.setStatus(InboundEventStatus.PROCESSED);
    entity.setJobId(jobId);
    entity.setProcessedAt(LocalDateTime.now());
    entity.setErrorMessage(null);
    inboundMessageEventRepository.save(entity);
  }

  @Override
  public void markFailed(String eventId, String errorMessage) {
    InboundMessageEvent entity = inboundMessageEventRepository.findById(eventId)
        .orElseThrow(() -> new IllegalStateException("Inbound event not found: " + eventId));
    entity.setStatus(InboundEventStatus.FAILED);
    entity.setProcessedAt(LocalDateTime.now());
    entity.setErrorMessage(errorMessage);
    inboundMessageEventRepository.save(entity);
  }
}
