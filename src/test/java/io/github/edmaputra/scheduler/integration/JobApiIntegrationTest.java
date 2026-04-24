package io.github.edmaputra.scheduler.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import io.github.edmaputra.scheduler.adapter.in.messaging.MessageEventListener;
import io.github.edmaputra.scheduler.adapter.in.quartz.JobExecutionCleanupJob;
import io.github.edmaputra.scheduler.adapter.out.messaging.MessagePublisher;
import io.github.edmaputra.scheduler.adapter.out.persistence.repository.JobExecutionRepository;
import io.github.edmaputra.scheduler.adapter.out.persistence.repository.JobRepository;
import io.github.edmaputra.scheduler.domain.Job;
import io.github.edmaputra.scheduler.domain.JobExecution;
import io.github.edmaputra.scheduler.domain.JobStatus;
import io.github.edmaputra.scheduler.domain.JobType;
import io.github.edmaputra.scheduler.dto.CreateCronJobRequest;
import io.github.edmaputra.scheduler.dto.CreateDelayedJobRequest;
import io.github.edmaputra.scheduler.dto.JobResponse;
import io.github.edmaputra.scheduler.dto.JobScheduleEvent;
import io.github.edmaputra.scheduler.dto.MessageProcessingResult;
import io.github.edmaputra.scheduler.dto.MessageProcessingStatus;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClient;

/**
 * Integration tests for Job API endpoints using H2 in-memory database
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class JobApiIntegrationTest {

  @TestConfiguration
  static class QuartzTestConfig {

    @Bean(name = "testScheduler")
    @Primary
    Scheduler scheduler() {
      return mock(Scheduler.class);
    }

    @Bean(name = "testMessagePublisher")
    @Primary
    MessagePublisher messagePublisher() {
      return mock(MessagePublisher.class);
    }
  }

  @LocalServerPort
  private int port;

  private RestClient restClient;

  @Autowired
  private JobExecutionCleanupJob jobExecutionCleanupJob;

  @Autowired
  private JobExecutionRepository jobExecutionRepository;

  @Autowired
  private JobRepository jobRepository;

  @Autowired
  private MessageEventListener messageEventListener;

  @BeforeEach
  void setUp() {
    restClient = RestClient.builder()
        .baseUrl("http://localhost:" + port)
        .build();
  }

  @Test
  void shouldCreateCronJob() {
    // Given
    CreateCronJobRequest request = CreateCronJobRequest.builder()
        .name("Test CRON Job")
        .description("Test description")
        .cronExpression("0 0 9 * * ?")
        .payload("{\"test\": \"data\"}")
        .createdBy("test-user")
        .build();

    // When
    JobResponse response = restClient.post()
        .uri("/api/jobs/cron")
        .contentType(MediaType.APPLICATION_JSON)
        .body(request)
        .retrieve()
        .body(JobResponse.class);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getName()).isEqualTo("Test CRON Job");
    assertThat(response.getType()).isEqualTo(JobType.CRON);
    assertThat(response.getStatus()).isEqualTo(JobStatus.PENDING);
    assertThat(response.getCronExpression()).isEqualTo("0 0 9 * * ?");
    assertThat(response.getId()).isNotNull();
  }

  @Test
  void shouldCreateDelayedJob() {
    // Given
    LocalDateTime futureTime = LocalDateTime.now().plusHours(2);
    CreateDelayedJobRequest request = CreateDelayedJobRequest.builder()
        .name("Test Delayed Job")
        .description("Test delayed job description")
        .scheduledTime(futureTime)
        .payload("{\"reminder\": \"test\"}")
        .createdBy("test-user")
        .build();

    // When
    JobResponse response = restClient.post()
        .uri("/api/jobs/delayed")
        .contentType(MediaType.APPLICATION_JSON)
        .body(request)
        .retrieve()
        .body(JobResponse.class);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getName()).isEqualTo("Test Delayed Job");
    assertThat(response.getType()).isEqualTo(JobType.DELAYED);
    assertThat(response.getStatus()).isEqualTo(JobStatus.PENDING);
    assertThat(response.getScheduledTime()).isNotNull();
    assertThat(response.getId()).isNotNull();
  }

  @Test
  void shouldGetJobById() {
    // Given - Create a job first
    CreateCronJobRequest createRequest = CreateCronJobRequest.builder()
        .name("Job to Retrieve")
        .description("Test description")
        .cronExpression("0 0 12 * * ?")
        .payload("{}")
        .createdBy("test-user")
        .build();

    JobResponse createdJob = restClient.post()
        .uri("/api/jobs/cron")
        .contentType(MediaType.APPLICATION_JSON)
        .body(createRequest)
        .retrieve()
        .body(JobResponse.class);

    String jobId = createdJob.getId().toString();

    // When
    JobResponse response = restClient.get()
        .uri("/api/jobs/" + jobId)
        .retrieve()
        .body(JobResponse.class);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getName()).isEqualTo("Job to Retrieve");
    assertThat(response.getId()).isEqualTo(createdJob.getId());
  }

  @Test
  void shouldGetAllJobs() {
    // Given - Create a couple of jobs
    CreateCronJobRequest request1 = CreateCronJobRequest.builder()
        .name("Job 1")
        .cronExpression("0 0 9 * * ?")
        .createdBy("test-user")
        .build();

    CreateCronJobRequest request2 = CreateCronJobRequest.builder()
        .name("Job 2")
        .cronExpression("0 0 10 * * ?")
        .createdBy("test-user")
        .build();

    restClient.post().uri("/api/jobs/cron").contentType(MediaType.APPLICATION_JSON).body(request1)
        .retrieve()
        .toBodilessEntity();
    restClient.post().uri("/api/jobs/cron").contentType(MediaType.APPLICATION_JSON).body(request2)
        .retrieve()
        .toBodilessEntity();

    // When
    JobResponse[] response = restClient.get()
        .uri("/api/jobs")
        .retrieve()
        .body(JobResponse[].class);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.length).isGreaterThanOrEqualTo(2);
  }

  @Test
  void shouldFilterJobsByStatus() {
    // When
    JobResponse[] response = restClient.get()
        .uri("/api/jobs?status=SCHEDULED")
        .retrieve()
        .body(JobResponse[].class);

    // Then
    assertThat(response).isNotNull();
    for (JobResponse job : response) {
      assertThat(job.getStatus()).isEqualTo(JobStatus.SCHEDULED);
    }
  }

  @Test
  void shouldFilterJobsByType() {
    // When
    JobResponse[] response = restClient.get()
        .uri("/api/jobs?type=CRON")
        .retrieve()
        .body(JobResponse[].class);

    // Then
    assertThat(response).isNotNull();
    for (JobResponse job : response) {
      assertThat(job.getType()).isEqualTo(JobType.CRON);
    }
  }

  @Test
  void shouldCancelJob() {
    // Given - Create a job first
    CreateCronJobRequest createRequest = CreateCronJobRequest.builder()
        .name("Job to Cancel")
        .description("Test description")
        .cronExpression("0 0 12 * * ?")
        .payload("{}")
        .createdBy("test-user")
        .build();

    JobResponse createdJob = restClient.post()
        .uri("/api/jobs/cron")
        .contentType(MediaType.APPLICATION_JSON)
        .body(createRequest)
        .retrieve()
        .body(JobResponse.class);

    String jobId = createdJob.getId().toString();

    // When
    restClient.delete()
        .uri("/api/jobs/" + jobId)
        .retrieve()
        .toBodilessEntity();

    // Then - Verify job is cancelled
    JobResponse response = restClient.get()
        .uri("/api/jobs/" + jobId)
        .retrieve()
        .body(JobResponse.class);

    assertThat(response).isNotNull();
    assertThat(response.getStatus()).isEqualTo(JobStatus.CANCELLED);
  }

  @Test
  void shouldStopJob() {
    // Given - Create a job first
    CreateCronJobRequest createRequest = CreateCronJobRequest.builder()
        .name("Job to Stop")
        .description("Test description")
        .cronExpression("0 0 12 * * ?")
        .payload("{}")
        .createdBy("test-user")
        .build();

    JobResponse createdJob = restClient.post()
        .uri("/api/jobs/cron")
        .contentType(MediaType.APPLICATION_JSON)
        .body(createRequest)
        .retrieve()
        .body(JobResponse.class);

    String jobId = createdJob.getId().toString();

    // When
    restClient.post()
        .uri("/api/jobs/" + jobId + "/stop")
        .retrieve()
        .toBodilessEntity();

    // Then - Verify job is stopped
    JobResponse response = restClient.get()
        .uri("/api/jobs/" + jobId)
        .retrieve()
        .body(JobResponse.class);

    assertThat(response).isNotNull();
    assertThat(response.getStatus()).isEqualTo(JobStatus.STOPPED);
  }

  @Test
  void shouldMarkStaleExecutionsAsTimeout() throws Exception {
    // Given - Create a job
    CreateCronJobRequest createRequest = CreateCronJobRequest.builder()
        .name("Job with Stale Execution")
        .description("Test description")
        .cronExpression("0 0 12 * * ?")
        .payload("{}")
        .createdBy("test-user")
        .build();

    JobResponse createdJob = restClient.post()
        .uri("/api/jobs/cron")
        .contentType(MediaType.APPLICATION_JSON)
        .body(createRequest)
        .retrieve()
        .body(JobResponse.class);

    // Create a stale execution (RUNNING status, started more than 1 hour ago)
    Job job = jobRepository.findById(createdJob.getId()).orElseThrow();
    LocalDateTime staleStartTime = LocalDateTime.now().minusHours(2);
    
    JobExecution staleExecution = JobExecution.builder()
        .job(job)
        .status(JobStatus.RUNNING)
        .startedAt(staleStartTime)
        .build();

    jobExecutionRepository.save(staleExecution);

    // When - Trigger cleanup via Quartz job
    jobExecutionCleanupJob.execute(null);

    // Then - Verify execution is marked as TIMEOUT
    JobExecution updatedExecution = jobExecutionRepository.findById(staleExecution.getId())
        .orElseThrow();

    assertThat(updatedExecution.getStatus()).isEqualTo(JobStatus.TIMEOUT);
    assertThat(updatedExecution.getCompletedAt()).isNotNull();
    assertThat(updatedExecution.getStaleSince()).isNotNull();
    assertThat(updatedExecution.getErrorMessage()).contains("timed out after one hour");
  }

  @Test
  void shouldCreateCronJobViaMockMessageAdapter() {
    JobScheduleEvent event = JobScheduleEvent.builder()
        .eventId("evt-integration-1")
        .correlationId("corr-integration-1")
        .jobType(JobType.CRON)
        .name("Message Triggered CRON")
        .description("Created via mock inbound adapter")
        .cronExpression("0 0 11 * * ?")
        .payload("{\"source\":\"message\"}")
        .createdBy("integration-test")
        .build();

    MessageProcessingResult response = messageEventListener.onJobScheduleEvent(event);

    assertThat(response).isNotNull();
    assertThat(response.getStatus()).isEqualTo(MessageProcessingStatus.PROCESSED);
    assertThat(response.getJobId()).isNotNull();

    Job persisted = jobRepository.findById(response.getJobId()).orElseThrow();
    assertThat(persisted.getName()).isEqualTo("Message Triggered CRON");
    assertThat(persisted.getType()).isEqualTo(JobType.CRON);
    assertThat(persisted.getStatus()).isEqualTo(JobStatus.PENDING);
  }

  @Test
  void shouldIgnoreDuplicateEventViaMockMessageAdapter() {
    JobScheduleEvent event = JobScheduleEvent.builder()
        .eventId("evt-integration-dup-1")
        .correlationId("corr-integration-dup")
        .jobType(JobType.CRON)
        .name("Message Triggered Duplicate")
        .description("Duplicate delivery simulation")
        .cronExpression("0 0 11 * * ?")
        .payload("{\"source\":\"message\"}")
        .createdBy("integration-test")
        .build();

    MessageProcessingResult first = messageEventListener.onJobScheduleEvent(event);
    MessageProcessingResult second = messageEventListener.onJobScheduleEvent(event);

    assertThat(first.getStatus()).isEqualTo(MessageProcessingStatus.PROCESSED);
    assertThat(second.getStatus()).isEqualTo(MessageProcessingStatus.DUPLICATE);
  }

  @Test
  void shouldNotMarkRecentExecutionsAsTimeout() throws Exception {
    // Given - Create a job
    CreateCronJobRequest createRequest = CreateCronJobRequest.builder()
        .name("Job with Recent Execution")
        .description("Test description")
        .cronExpression("0 0 12 * * ?")
        .payload("{}")
        .createdBy("test-user")
        .build();

    JobResponse createdJob = restClient.post()
        .uri("/api/jobs/cron")
        .contentType(MediaType.APPLICATION_JSON)
        .body(createRequest)
        .retrieve()
        .body(JobResponse.class);

    // Create a recent execution (RUNNING status, started less than 1 hour ago)
    Job job = jobRepository.findById(createdJob.getId()).orElseThrow();
    LocalDateTime recentStartTime = LocalDateTime.now().minusMinutes(30);
    
    JobExecution recentExecution = JobExecution.builder()
        .job(job)
        .status(JobStatus.RUNNING)
        .startedAt(recentStartTime)
        .build();

    jobExecutionRepository.save(recentExecution);

    // When - Trigger cleanup via Quartz job
    jobExecutionCleanupJob.execute(null);

    // Then - Verify execution is still RUNNING (not marked as TIMEOUT)
    JobExecution updatedExecution = jobExecutionRepository.findById(recentExecution.getId())
        .orElseThrow();

    assertThat(updatedExecution.getStatus()).isEqualTo(JobStatus.RUNNING);
    assertThat(updatedExecution.getCompletedAt()).isNull();
    assertThat(updatedExecution.getErrorMessage()).isNull();
  }
}
