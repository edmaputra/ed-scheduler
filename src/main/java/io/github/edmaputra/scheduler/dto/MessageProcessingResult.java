package io.github.edmaputra.scheduler.dto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageProcessingResult {

  private String eventId;
  private String correlationId;
  private UUID jobId;
  private MessageProcessingStatus status;
  private String details;
}
