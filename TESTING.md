# Testing the Scheduler API

## Prerequisites

Since Docker is not available in your environment, you'll need to either:

1. **Install Docker** to run PostgreSQL via Docker Compose
2. **Install PostgreSQL locally** and update the connection settings
3. **Use a remote PostgreSQL instance**

## Option 1: Using Docker (Recommended)

If you install Docker, you can start the database with:

```bash
docker compose up -d
```

Then run the application:

```bash
./gradlew bootRun
```

## Option 2: Local PostgreSQL

If you have PostgreSQL installed locally:

1. Create a database:
   ```sql
   CREATE DATABASE scheduler;
   CREATE USER scheduler WITH PASSWORD 'scheduler';
   GRANT ALL PRIVILEGES ON DATABASE scheduler TO scheduler;
   ```

2. Update `src/main/resources/application.properties` if needed (current settings should work with default PostgreSQL)

3. Run the application:
   ```bash
   ./gradlew bootRun
   ```

## Testing the API

Once the application is running, you can test the endpoints using the examples in `api-examples.http` or with curl:

### 1. Create a CRON Job

```bash
curl -X POST http://localhost:8080/api/jobs/cron \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Daily Report",
    "description": "Generate daily reports at 9 AM",
    "cronExpression": "0 0 9 * * ?",
    "payload": "{\"type\": \"daily\"}",
    "createdBy": "admin"
  }'
```

### 2. Create a Delayed Job

```bash
curl -X POST http://localhost:8080/api/jobs/delayed \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Meeting Reminder",
    "description": "Remind about meeting",
    "scheduledTime": "2026-02-14T15:30:00",
    "payload": "{\"userId\": \"123\"}",
    "createdBy": "system"
  }'
```

### 3. Get All Jobs

```bash
curl http://localhost:8080/api/jobs
```

### 4. Get Job by ID

```bash
curl http://localhost:8080/api/jobs/{job-id}
```

### 5. Filter Jobs by Status

```bash
curl http://localhost:8080/api/jobs?status=SCHEDULED
```

### 6. Filter Jobs by Type

```bash
curl http://localhost:8080/api/jobs?type=CRON
```

### 7. Cancel a Job

```bash
curl -X DELETE http://localhost:8080/api/jobs/{job-id}
```

## Integration Tests

Integration tests are available in `src/test/java/io/github/edmaputra/scheduler/integration/JobApiIntegrationTest.java`.

**Note**: The integration tests use Testcontainers which requires Docker. If Docker is not available, you can:

1. Skip integration tests and test manually using the API
2. Install Docker to run the automated tests
3. Modify the tests to use an embedded H2 database (not recommended for production-like testing)

To run integration tests (requires Docker):

```bash
./gradlew test --tests JobApiIntegrationTest
```

## Verification Checklist

- [ ] PostgreSQL is running
- [ ] Application starts successfully
- [ ] Can create CRON jobs
- [ ] Can create delayed jobs
- [ ] Can retrieve jobs by ID
- [ ] Can list all jobs
- [ ] Can filter jobs by status
- [ ] Can filter jobs by type
- [ ] Can cancel jobs
- [ ] Jobs are persisted in database
- [ ] Quartz scheduler is executing jobs
