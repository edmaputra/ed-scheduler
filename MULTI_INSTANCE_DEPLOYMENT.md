# Multi-Instance/Multi-Replica Deployment Guide

This document describes the scheduler's readiness for multi-instance deployments and necessary configurations.

## Overview

The scheduler is designed to support multiple instances/replicas running concurrently with proper coordination mechanisms to prevent duplicate job executions and ensure distributed consistency.

## Architecture Components

### 1. Quartz Clustering ✅

**Status**: Production Ready

Quartz Scheduler is configured with JDBC-based job store and clustering enabled.

**Configuration**:
```properties
spring.quartz.job-store-type=jdbc
spring.quartz.properties.org.quartz.jobStore.class=org.springframework.scheduling.quartz.LocalDataSourceJobStore
spring.quartz.properties.org.quartz.jobStore.driverDelegateClass=org.quartz.impl.jdbcjobstore.PostgreSQLDelegate
spring.quartz.properties.org.quartz.jobStore.isClustered=true
spring.quartz.properties.org.quartz.jobStore.clusterCheckinInterval=20000
```

**How it works**:
- All instances register themselves with the Quartz cluster
- Jobs are stored in the database and shared across all instances
- Quartz uses pessimistic locking to ensure a job trigger is executed on only one instance
- Each instance checks in with the cluster every 20 seconds
- If an instance fails, its jobs are automatically reassigned to other instances

**Benefits**:
- No duplicate CRON job executions across instances
- Automatic failover support
- Load distribution across instances

### 2. Distributed Scheduled Tasks Locking ✅

**Status**: Production Ready (added via ShedLock)

Scheduled tasks are protected with ShedLock to ensure only one instance executes them.

**Affected Tasks**:
- `JobExecutionCleanupService.markStaleRunningExecutionsAsTimeout()` - Marks executions older than 1 hour as TIMEOUT

**Configuration**:
```java
@Scheduled(fixedDelay = 300000)  // Runs every 5 minutes
@SchedulerLock(name = "markStaleRunningExecutionsAsTimeout", 
               lockAtMostFor = "4m59s",    // Max lock duration
               lockAtLeastFor = "30s")      // Min lock duration before release
public void markStaleRunningExecutionsAsTimeout() { ... }
```

**How it works**:
- Before execution, ShedLock acquires a distributed lock via the database
- Lock is held in the `shedlock` table
- Only the instance holding the lock executes the task
- Lock is released after task completion or timeout
- `lockAtLeastFor` prevents rapid re-execution if task completes quickly
- `lockAtMostFor` prevents deadlocks if instance crashes while holding lock

**Benefits**:
- No duplicate cleanup task executions
- Ensures efficient resource usage
- Prevents race conditions on execution state updates

### 3. Database Consistency ✅

**Status**: Production Ready

Database-level optimistic and pessimistic locking ensures consistency.

**Mechanisms**:
- **Pessimistic Locking (Quartz)**: Database row locks during job trigger acquisition
- **Optimistic Locking (JobExecution)**: Version field prevents concurrent updates
  ```java
  @Version
  private Long version;
  ```
- **Transactions**: All state-changing operations are transactional

## Multi-Instance Deployment Checklist

- [ ] **Database Setup**
  - PostgreSQL instance accessible from all application instances
  - Database user has permissions to create/modify tables (for Liquibase migrations)
  - Liquibase has run successfully (creates Quartz and ShedLock tables)

- [ ] **Network Requirements**
  - All instances can connect to the shared database
  - Consider connection pooling for high instance counts
  - Database connection timeout should be reasonable (not too short)

- [ ] **Clock Synchronization**
  - All instances should have synchronized system clocks (NTP)
  - Critical for CRON triggers and scheduled tasks
  - Recommend: within 100ms of each other

- [ ] **Configuration Verification**
  ```properties
  # Verify Quartz clustering is enabled
  spring.quartz.job-store-type=jdbc
  spring.quartz.properties.org.quartz.jobStore.isClustered=true
  
  # Verify ShedLock provider is configured
  spring.liquibase.enabled=true  # Ensures shedlock table is created
  ```

- [ ] **Database Tables Created**
  - `qrtz_*` - Quartz scheduler tables (auto-created by Liquibase)
  - `shedlock` - ShedLock lock management table
  - `job` - Application job records
  - `job_executions` - Execution history

- [ ] **Monitoring Setup**
  - Monitor Quartz cluster registration via logs
  - Track ShedLock acquisition/release for scheduled tasks
  - Monitor database connection pool usage

## Kubernetes Deployment Example

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: scheduler
spec:
  replicas: 3  # Multiple instances
  selector:
    matchLabels:
      app: scheduler
  template:
    metadata:
      labels:
        app: scheduler
    spec:
      containers:
      - name: scheduler
        image: scheduler:latest
        env:
        - name: SPRING_DATASOURCE_URL
          value: "jdbc:postgresql://postgres-db:5432/scheduler"
        - name: SPRING_DATASOURCE_USERNAME
          valueFrom:
            secretKeyRef:
              name: db-credentials
              key: username
        - name: SPRING_DATASOURCE_PASSWORD
          valueFrom:
            secretKeyRef:
              name: db-credentials
              key: password
        - name: JAVA_OPTS
          value: "-XX:+UseG1GC -Xmx512m -Xms256m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 20
          periodSeconds: 5
        resources:
          requests:
            memory: "256Mi"
            cpu: "100m"
          limits:
            memory: "512Mi"
            cpu: "500m"
```

## Potential Issues & Solutions

### Issue 1: Clock Skew Between Instances
**Problem**: Instances have significantly different system times
**Impact**: CRON triggers may execute at wrong times or duplicate
**Solution**: Enforce NTP synchronization; monitor clock differences

### Issue 2: Database Connection Exhaustion
**Problem**: Too many instances creating connection pools
**Impact**: Database connection limit exceeded, new requests fail
**Solution**: 
- Configure appropriate connection pool sizes per instance
- Use PgBouncer or similar connection pooler for centralized management
- Monitor connection pool usage

### Issue 3: Long-Running Scheduled Tasks
**Problem**: Cleanup task takes longer than 5 minutes
**Impact**: Task may execute on next instance before previous completes
**Solution**:
- Increase `lockAtMostFor` if needed
- Optimize query performance
- Monitor cleanup task execution time

### Issue 4: Instance Crash During Lock Hold
**Problem**: Instance crashes while holding ShedLock
**Impact**: Lock prevents other instances from running task
**Solution**:
- ShedLock automatically releases lock after `lockAtMostFor` duration
- Default: 4m59s (task runs every 5 minutes)
- Adjust based on expected task duration

## Performance Considerations

### Quartz Cluster Check-in Interval
```properties
spring.quartz.properties.org.quartz.jobStore.clusterCheckinInterval=20000  # 20 seconds
```
- Smaller interval: Faster failover detection, more database queries
- Larger interval: Less overhead, slower failover
- Recommendation: 15-30 seconds for most deployments

### ShedLock Configuration
```java
@SchedulerLock(name = "...", 
               lockAtMostFor = "4m59s",  // Should be < task frequency
               lockAtLeastFor = "30s")    // Prevents rapid re-execution
```
- Adjust `lockAtLeastFor` based on database load preferences
- Keep `lockAtMostFor` slightly less than task frequency to prevent double execution

### Database Connection Pool
```properties
spring.datasource.hikari.maximum-pool-size=10  # Per instance
spring.datasource.hikari.minimum-idle=2
spring.datasource.hikari.connection-timeout=20000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000
```
- Adjust based on instance count and expected load
- Total pool size = instances × maximum-pool-size

## Monitoring & Logging

### Key Metrics to Monitor
1. **Quartz Cluster**
   - Number of registered instances
   - Job execution count and failures
   - Cluster check-in failures

2. **ShedLock**
   - Lock acquisition success rate
   - Lock hold duration
   - Lock release latency

3. **Database**
   - Connection pool usage
   - Query performance (especially Quartz queries)
   - Transaction duration

### Log Patterns to Watch
```
# Successful Quartz cluster registration
INFO  QuartzScheduler: Quartz scheduler version: ...
INFO  QuartzScheduler: Scheduler SchedulerInstance_... started.
INFO  JobStoreSupport: The instance with id: ... belongs to a cluster.

# Successful ShedLock
INFO  Cleanup task completed: marked N stale executions as TIMEOUT

# Problems to watch for
WARN  ... Lock could not be acquired ...
ERROR ... Cluster check-in failed ...
```

## Scaling Guidelines

| Instances | Notes |
|-----------|-------|
| 1-2 | Minimal overhead; monitoring less critical |
| 3-5 | Standard HA setup; monitor cluster health |
| 5-10 | Consider connection pooler; monitor database performance |
| 10+ | Use dedicated connection pooler; consider PgBouncer |

## Testing Multi-Instance Setup

### Local Testing
```bash
# Terminal 1
SPRING_QUARTZ_PROPERTIES_ORG_QUARTZ_SCHEDULER_INSTANCE_ID=instance1 \
java -jar scheduler.jar

# Terminal 2
SPRING_QUARTZ_PROPERTIES_ORG_QUARTZ_SCHEDULER_INSTANCE_ID=instance2 \
java -jar scheduler.jar

# Terminal 3
SPRING_QUARTZ_PROPERTIES_ORG_QUARTZ_SCHEDULER_INSTANCE_ID=instance3 \
java -jar scheduler.jar
```

### Integration Tests
- Verify no duplicate job executions
- Verify cleanup task runs on only one instance
- Verify failover when an instance stops

## Future Enhancements

1. **Message Publishing**: Ensure idempotent message delivery for multi-instance events
2. **Actuator Endpoints**: Add cluster status endpoint to check Quartz cluster membership
3. **Metrics**: Expose Quartz and ShedLock metrics to Micrometer
4. **Observability**: Add distributed tracing for cross-instance job execution tracking
