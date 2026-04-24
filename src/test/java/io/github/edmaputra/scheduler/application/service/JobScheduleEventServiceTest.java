package io.github.edmaputra.scheduler.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.edmaputra.scheduler.application.port.in.JobManagementUseCase;
import io.github.edmaputra.scheduler.application.port.out.InboundEventStorePort;
import io.github.edmaputra.scheduler.domain.JobType;
import io.github.edmaputra.scheduler.dto.CreateCronJobRequest;
import io.github.edmaputra.scheduler.dto.CreateDelayedJobRequest;
import io.github.edmaputra.scheduler.dto.JobResponse;
import io.github.edmaputra.scheduler.dto.JobScheduleEvent;
import io.github.edmaputra.scheduler.dto.MessageProcessingResult;
import io.github.edmaputra.scheduler.dto.MessageProcessingStatus;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JobScheduleEventServiceTest {

  @Mock
  private JobManagementUseCase jobManagementUseCase;

  @Mock
  private InboundEventStorePort inboundEventStorePort;

  private JobScheduleEventService service;

  @BeforeEach
  void setUp() {
    service = new JobScheduleEventService(jobManagementUseCase, inboundEventStorePort);
  }

  @Test
  void shouldHandleCronEvent() {
    JobScheduleEvent event = JobScheduleEvent.builder()
      .eventId("evt-cron-1")
      .correlationId("corr-1")
        .jobType(JobType.CRON)
        .name("cron-from-message")
        .description("cron description")
        .cronExpression("0 0 9 * * ?")
        .payload("{\"hello\":\"world\"}")
        .topic("jobs-topic")
        .createdBy("message-user")
        .build();

    when(inboundEventStorePort.registerProcessing(event.getEventId(), event.getCorrelationId()))
      .thenReturn(true);
    JobResponse expected = JobResponse.builder().id(UUID.randomUUID()).name(event.getName()).build();
    when(jobManagementUseCase.createCronJob(any(CreateCronJobRequest.class))).thenReturn(expected);

    MessageProcessingResult actual = service.handle(event);

    assertThat(actual.getStatus()).isEqualTo(MessageProcessingStatus.PROCESSED);
    assertThat(actual.getJobId()).isEqualTo(expected.getId());
    ArgumentCaptor<CreateCronJobRequest> captor = ArgumentCaptor.forClass(CreateCronJobRequest.class);
    verify(jobManagementUseCase).createCronJob(captor.capture());
    verify(jobManagementUseCase, never()).createDelayedJob(any(CreateDelayedJobRequest.class));
    verify(inboundEventStorePort).markProcessed(event.getEventId(), expected.getId());

    CreateCronJobRequest request = captor.getValue();
    assertThat(request.getName()).isEqualTo(event.getName());
    assertThat(request.getDescription()).isEqualTo(event.getDescription());
    assertThat(request.getCronExpression()).isEqualTo(event.getCronExpression());
    assertThat(request.getPayload()).isEqualTo(event.getPayload());
    assertThat(request.getTopic()).isEqualTo(event.getTopic());
    assertThat(request.getCreatedBy()).isEqualTo(event.getCreatedBy());
  }

  @Test
  void shouldHandleDelayedEvent() {
    LocalDateTime scheduledTime = LocalDateTime.now().plusMinutes(30);
    JobScheduleEvent event = JobScheduleEvent.builder()
      .eventId("evt-delayed-1")
      .correlationId("corr-2")
        .jobType(JobType.DELAYED)
        .name("delayed-from-message")
        .description("delayed description")
        .scheduledTime(scheduledTime)
        .payload("{\"reminder\":\"yes\"}")
        .topic("reminders")
        .createdBy("message-user")
        .build();

    when(inboundEventStorePort.registerProcessing(event.getEventId(), event.getCorrelationId()))
      .thenReturn(true);
    JobResponse expected = JobResponse.builder().id(UUID.randomUUID()).name(event.getName()).build();
    when(jobManagementUseCase.createDelayedJob(any(CreateDelayedJobRequest.class))).thenReturn(expected);

    MessageProcessingResult actual = service.handle(event);

    assertThat(actual.getStatus()).isEqualTo(MessageProcessingStatus.PROCESSED);
    assertThat(actual.getJobId()).isEqualTo(expected.getId());
    ArgumentCaptor<CreateDelayedJobRequest> captor = ArgumentCaptor.forClass(
        CreateDelayedJobRequest.class);
    verify(jobManagementUseCase).createDelayedJob(captor.capture());
    verify(jobManagementUseCase, never()).createCronJob(any(CreateCronJobRequest.class));
    verify(inboundEventStorePort).markProcessed(event.getEventId(), expected.getId());

    CreateDelayedJobRequest request = captor.getValue();
    assertThat(request.getName()).isEqualTo(event.getName());
    assertThat(request.getDescription()).isEqualTo(event.getDescription());
    assertThat(request.getScheduledTime()).isEqualTo(event.getScheduledTime());
    assertThat(request.getPayload()).isEqualTo(event.getPayload());
    assertThat(request.getTopic()).isEqualTo(event.getTopic());
    assertThat(request.getCreatedBy()).isEqualTo(event.getCreatedBy());
  }

  @Test
  void shouldFailWhenEventIsNull() {
    assertThatThrownBy(() -> service.handle(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Job schedule event cannot be null");
  }

  @Test
  void shouldFailWhenTypeIsNull() {
    JobScheduleEvent event = JobScheduleEvent.builder().eventId("evt-missing-type")
        .name("missing-type").build();

    assertThatThrownBy(() -> service.handle(event))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Job type is required");
  }

  @Test
  void shouldFailWhenEventIdIsMissing() {
    JobScheduleEvent event = JobScheduleEvent.builder().jobType(JobType.CRON).name("missing-id")
        .build();

    assertThatThrownBy(() -> service.handle(event))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Event ID is required");
  }

  @Test
  void shouldReturnDuplicateWhenEventAlreadyRegistered() {
    JobScheduleEvent event = JobScheduleEvent.builder()
        .eventId("evt-dup-1")
        .correlationId("corr-dup")
        .jobType(JobType.CRON)
        .name("duplicate")
        .build();
    when(inboundEventStorePort.registerProcessing(event.getEventId(), event.getCorrelationId()))
        .thenReturn(false);

    MessageProcessingResult result = service.handle(event);

    assertThat(result.getStatus()).isEqualTo(MessageProcessingStatus.DUPLICATE);
    verify(jobManagementUseCase, never()).createCronJob(any(CreateCronJobRequest.class));
    verify(jobManagementUseCase, never()).createDelayedJob(any(CreateDelayedJobRequest.class));
  }
}
