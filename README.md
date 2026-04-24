# Scheduler Service

A robust job scheduling service built with Spring Boot and Quartz Scheduler, supporting both CRON-based recurring jobs and one-time delayed jobs.

## Features

- **CRON-based Scheduling**: Schedule recurring jobs using CRON expressions
- **Delayed Jobs**: Schedule one-time jobs to execute at a specific time (like reminders)
- **REST API**: Full REST API for job management
- **Message Event Listener**: Pluggable interface for message-based job scheduling
- **Hexagonal Architecture**: Clear separation between use cases, ports, and adapters
- **Persistent Storage**: Jobs and execution history stored in PostgreSQL
- **Quartz Integration**: Leverages Quartz Scheduler for reliable job execution
- **Job Tracking**: Track job status and execution history
- **Clustered Support**: Quartz clustering enabled for high availability

## Tech Stack

- Java 25
- Spring Boot 4.0.2
- Spring Data JPA
- Quartz Scheduler
- PostgreSQL
- Liquibase
- Lombok

## Getting Started

### Prerequisites

- Java 25
- Docker and Docker Compose (for PostgreSQL)

### Running the Application

1. **Start PostgreSQL**:
   ```bash
   podman-compose up -d
   ```

2. **Build the application**:
   ```bash
   ./gradlew build
   ```

3. **Run the application**:
   ```bash
   ./gradlew bootRun
   ```

The application will start on `http://localhost:8080`.

## API Documentation

### Create a CRON Job

Schedule a recurring job using a CRON expression.

**Endpoint**: `POST /api/jobs/cron`

**Request Body**:
```json
{
  "name": "Daily Report Generator",
  "description": "Generates daily reports at 9 AM",
  "cronExpression": "0 0 9 * * ?",
  "payload": "{\"reportType\": \"daily\", \"recipients\": [\"admin@example.com\"]}",
  "createdBy": "admin"
}
```

**Response**:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Daily Report Generator",
  "description": "Generates daily reports at 9 AM",
  "type": "CRON",
  "status": "SCHEDULED",
  "cronExpression": "0 0 9 * * ?",
  "scheduledTime": null,
  "payload": "{\"reportType\": \"daily\", \"recipients\": [\"admin@example.com\"]}",
  "createdAt": "2026-02-13T14:30:00",
  "updatedAt": "2026-02-13T14:30:00",
  "createdBy": "admin"
}
```

### Create a Delayed Job

Schedule a one-time job to execute at a specific time.

**Endpoint**: `POST /api/jobs/delayed`

**Request Body**:
```json
{
  "name": "Meeting Reminder",
  "description": "Remind user about meeting",
  "scheduledTime": "2026-02-14T15:30:00",
  "payload": "{\"userId\": \"123\", \"message\": \"Meeting in 30 minutes\"}",
  "createdBy": "system"
}
```

**Response**:
```json
{
  "id": "660e8400-e29b-41d4-a716-446655440001",
  "name": "Meeting Reminder",
  "description": "Remind user about meeting",
  "type": "DELAYED",
  "status": "SCHEDULED",
  "cronExpression": null,
  "scheduledTime": "2026-02-14T15:30:00",
  "payload": "{\"userId\": \"123\", \"message\": \"Meeting in 30 minutes\"}",
  "createdAt": "2026-02-13T14:30:00",
  "updatedAt": "2026-02-13T14:30:00",
  "createdBy": "system"
}
```

### Get Job by ID

**Endpoint**: `GET /api/jobs/{id}`

**Response**: Same as create job response

### List All Jobs

**Endpoint**: `GET /api/jobs`

**Query Parameters**:
- `status` (optional): Filter by job status (SCHEDULED, RUNNING, COMPLETED, FAILED, CANCELLED)
- `type` (optional): Filter by job type (CRON, DELAYED)

**Examples**:
- Get all jobs: `GET /api/jobs`
- Get scheduled jobs: `GET /api/jobs?status=SCHEDULED`
- Get CRON jobs: `GET /api/jobs?type=CRON`

### Cancel a Job

**Endpoint**: `DELETE /api/jobs/{id}`

**Response**: `204 No Content`

## CRON Expression Examples

- `0 0 9 * * ?` - Every day at 9:00 AM
- `0 */15 * * * ?` - Every 15 minutes
- `0 0 12 * * MON-FRI` - Every weekday at noon
- `0 0 0 1 * ?` - First day of every month at midnight
- `0 0 8-17 * * ?` - Every hour between 8 AM and 5 PM

## Message Event Listener

Inbound messaging uses a shared adapter-in contract so mock and production listeners expose the same method. The current implementation includes:

- `MockMessageEventListener` for test/local flows without a broker
- `KafkaMessageEventListener` as a production-shaped adapter that can be connected to a real Kafka listener later

### Interface

```java
public interface MessageEventListener {
  MessageProcessingResult onJobScheduleEvent(JobScheduleEvent event);
}
```

### Example Implementation (Kafka-shaped adapter)

```java
@Component
@ConditionalOnProperty(name = "scheduler.messaging.inbound.provider", havingValue = "kafka")
public class KafkaMessageEventListener implements MessageEventListener {

  private final JobScheduleEventUseCase jobScheduleEventUseCase;

  @Override
  public MessageProcessingResult onJobScheduleEvent(JobScheduleEvent event) {
    return jobScheduleEventUseCase.handle(event);
    }
}
```

### Provider Selection

Choose the active inbound adapter with:

```properties
scheduler.messaging.inbound.provider=none
```

Supported values:

- `none`: inbound messaging adapter disabled
- `mock`: enable mock adapter-in (used in test profile)
- `kafka`: enable Kafka-shaped adapter-in

### JobScheduleEvent Format

```json
{
  "eventId": "evt-0001",
  "correlationId": "corr-2026-04-17-001",
  "jobType": "CRON",
  "name": "Automated Backup",
  "description": "Daily database backup",
  "cronExpression": "0 0 2 * * ?",
  "scheduledTime": null,
  "payload": "{\"database\": \"production\"}",
  "createdBy": "system"
}
```

## Database Schema

The application uses Liquibase for database migrations. The schema includes:

- `jobs` - Stores job definitions
- `job_executions` - Stores job execution history
- `qrtz_*` - Quartz scheduler tables

## Configuration

Key configuration properties in `application.properties`:

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/scheduler
spring.datasource.username=scheduler
spring.datasource.password=scheduler

# Quartz
spring.quartz.job-store-type=jdbc
spring.quartz.properties.org.quartz.jobStore.isClustered=true
spring.quartz.properties.org.quartz.threadPool.threadCount=10
```

## Multi-Instance Deployment

The application is designed to run safely on multiple replicas as long as they share the same PostgreSQL database and Quartz JDBC job store.

### What Makes It Safe

- Quartz clustering is enabled through JDBC-backed job storage.
- Scheduled job triggers are acquired by only one instance at a time.
- The cleanup job is scheduled as a Quartz job, so it is also coordinated by the cluster.
- System jobs do not create `JobExecution` records, so there is no feedback loop.

### Required Settings

```properties
spring.quartz.job-store-type=jdbc
spring.quartz.properties.org.quartz.jobStore.class=org.springframework.scheduling.quartz.LocalDataSourceJobStore
spring.quartz.properties.org.quartz.jobStore.driverDelegateClass=org.quartz.impl.jdbcjobstore.PostgreSQLDelegate
spring.quartz.properties.org.quartz.jobStore.isClustered=true
spring.quartz.properties.org.quartz.jobStore.clusterCheckinInterval=20000
```

### Deployment Checklist

- Use one shared PostgreSQL database for all instances.
- Make sure Liquibase has created the Quartz tables.
- Keep system clocks reasonably synchronized across instances.
- Confirm each instance uses the same Quartz configuration.

### What Runs in the Cluster

- User-defined CRON jobs.
- One-time delayed jobs.
- `JobExecutionCleanupJob`, which marks stale executions as `TIMEOUT`.

### Notes

- If you add future scheduled maintenance tasks, prefer Quartz jobs so the cluster continues to own coordination.

## Architecture

The project now uses **Hexagonal Architecture (Ports and Adapters)**.

Core business behavior is implemented as use cases and ports in the application layer. Infrastructure concerns (database, Quartz, messaging, REST, listeners) are implemented as adapters around the core.

### Layer Overview

1. **Domain**
  - Contains business entities and enums
  - Examples: `Job`, `JobExecution`, `JobStatus`, `JobType`

2. **Application (Use Cases + Ports)**
  - Inbound ports define what the system can do
    - `JobManagementUseCase`
    - `ScheduledJobExecutionUseCase`
  - Outbound ports define required external capabilities
    - `JobStorePort`
    - `JobExecutionStorePort`
    - `JobSchedulerPort`
    - `MessageDispatchPort`
  - Application services implement inbound ports
    - `JobManagementService`
    - `ScheduledJobExecutionService`

3. **Inbound Adapters**
  - Convert external input to use-case calls
  - `adapter/in/web/JobController` (HTTP)
  - `adapter/in/quartz/ScheduledJob` (job execution entrypoint)
  - `adapter/in/quartz/JobExecutionCleanupJob` (system cleanup job)

4. **Outbound Adapters**
  - Implement outbound ports for specific technologies
  - `adapter/out/persistence/JpaJobStoreAdapter`
  - `adapter/out/persistence/JpaJobExecutionStoreAdapter`
  - `adapter/out/persistence/repository/JobRepository`
  - `adapter/out/persistence/repository/JobExecutionRepository`
  - `adapter/out/scheduler/QuartzJobSchedulerAdapter`
  - `adapter/out/messaging/MessagePublisherAdapter`
  - `adapter/out/messaging/DefaultMessagePublisher`
  - `adapter/out/messaging/MessagePublisherConfiguration`

### Package Structure

```text
src/main/java/io/github/edmaputra/scheduler
├── application
│   ├── port
│   │   ├── in
│   │   └── out
│   └── service
├── config
├── adapter
│   ├── in
│   │   ├── quartz
│   │   └── web
│   └── out
│       ├── messaging
│       ├── persistence
│       │   └── repository
│       └── scheduler
├── domain
└── dto
```

### Dependency Direction

- Inbound adapters -> inbound ports (application)
- Application services -> outbound ports
- Outbound adapters -> infrastructure frameworks
- Domain has no dependency on adapters

This keeps business logic testable and independent from specific transport, database, or scheduler implementations.

```
Inbound Adapters
  (REST, Message Handler, Quartz Job Entrypoint)
        |
        v
Application Inbound Ports (Use Cases)
        |
        v
Application Services (Core Orchestration)
        |
        v
Application Outbound Ports
        |
        v
Outbound Adapters (JPA, Quartz, Messaging)
        |
        v
External Systems (PostgreSQL, Quartz, Message Broker)
```

## Testing Strategy

- **Production runtime** uses PostgreSQL + Quartz JDBC job store from `src/main/resources/application.properties`.
- **Integration tests** use H2 with profile `test` from `src/test/resources/application-test.properties`.

Notes about tests:

- API integration tests run with H2 in-memory database.
- Quartz auto-configuration is excluded in test profile so tests focus on API + persistence behavior.
- Test configuration provides mocked Scheduler and MessagePublisher beans to satisfy wiring while keeping integration tests lightweight.

## Job Lifecycle

1. **SCHEDULED** - Job is created and scheduled
2. **RUNNING** - Job is currently executing
3. **COMPLETED** - Job finished successfully
4. **FAILED** - Job execution failed
5. **CANCELLED** - Job was cancelled before completion

## Development

### Build

```bash
./gradlew build
```

### Run Tests

```bash
./gradlew test
```

### Clean Build

```bash
./gradlew clean build
```

## License

This project is licensed under the MIT License.
