package io.github.edmaputra.scheduler.integration;

import io.github.edmaputra.scheduler.domain.JobStatus;
import io.github.edmaputra.scheduler.domain.JobType;
import io.github.edmaputra.scheduler.dto.CreateCronJobRequest;
import io.github.edmaputra.scheduler.dto.CreateDelayedJobRequest;
import io.github.edmaputra.scheduler.dto.JobResponse;
import io.github.edmaputra.scheduler.adapter.out.messaging.MessagePublisher;
import org.quartz.Scheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Integration tests for Job API endpoints using H2 in-memory database
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class JobApiIntegrationTest {

        @TestConfiguration
        static class QuartzTestConfig {
                @Bean
                Scheduler scheduler() {
                        return mock(Scheduler.class);
                }

                @Bean
                MessagePublisher messagePublisher() {
                        return mock(MessagePublisher.class);
                }
        }

    @LocalServerPort
    private int port;

    private RestClient restClient;

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
        assertThat(response.getStatus()).isEqualTo(JobStatus.SCHEDULED);
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
        assertThat(response.getStatus()).isEqualTo(JobStatus.SCHEDULED);
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

        restClient.post().uri("/api/jobs/cron").contentType(MediaType.APPLICATION_JSON).body(request1).retrieve()
                .toBodilessEntity();
        restClient.post().uri("/api/jobs/cron").contentType(MediaType.APPLICATION_JSON).body(request2).retrieve()
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
}
