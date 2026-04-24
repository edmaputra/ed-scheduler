package io.github.edmaputra.scheduler.adapter.in.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.edmaputra.scheduler.application.port.in.JobScheduleEventUseCase;
import io.github.edmaputra.scheduler.domain.JobType;
import io.github.edmaputra.scheduler.dto.JobScheduleEvent;
import io.github.edmaputra.scheduler.dto.MessageProcessingResult;
import io.github.edmaputra.scheduler.dto.MessageProcessingStatus;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class KafkaMessageEventListenerTest {

  @Mock
  private JobScheduleEventUseCase jobScheduleEventUseCase;

  private KafkaMessageEventListener listener;

  @BeforeEach
  void setUp() {
    listener = new KafkaMessageEventListener(jobScheduleEventUseCase);
  }

  @Test
  void shouldDelegateToUseCaseWithSameContractMethod() {
    JobScheduleEvent event = JobScheduleEvent.builder()
      .eventId("evt-kafka-test")
        .jobType(JobType.DELAYED)
        .name("kafka-contract")
        .build();
    MessageProcessingResult expected = MessageProcessingResult.builder()
      .eventId(event.getEventId())
      .jobId(UUID.randomUUID())
      .status(MessageProcessingStatus.PROCESSED)
      .build();

    when(jobScheduleEventUseCase.handle(event)).thenReturn(expected);

    MessageProcessingResult actual = listener.onJobScheduleEvent(event);

    assertThat(actual).isEqualTo(expected);
    verify(jobScheduleEventUseCase).handle(event);
  }
}
