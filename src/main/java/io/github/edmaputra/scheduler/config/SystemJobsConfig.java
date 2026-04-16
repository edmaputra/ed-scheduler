package io.github.edmaputra.scheduler.config;

import io.github.edmaputra.scheduler.adapter.in.quartz.JobExecutionCleanupJob;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for system maintenance jobs scheduled via Quartz
 * 
 * These jobs run on a schedule and are coordinated across multiple instances
 * through Quartz clustering. Only one instance will execute each job at a time.
 */
@Configuration
public class SystemJobsConfig {

  /**
   * Job detail for the job execution cleanup task
   * Runs every 5 minutes to detect and mark stale executions as timeout
   */
  @Bean
  public JobDetail jobExecutionCleanupJobDetail() {
    return JobBuilder.newJob(JobExecutionCleanupJob.class)
        .withIdentity("job-execution-cleanup", "system-jobs")
        .withDescription("Cleanup stale job executions and mark as TIMEOUT")
        .storeDurably()
        .build();
  }

  /**
   * Trigger for the job execution cleanup job
   * Runs every 5 minutes (cron: 0 at every 5th minute)
   */
  @Bean
  public Trigger jobExecutionCleanupTrigger(JobDetail jobExecutionCleanupJobDetail) {
    return TriggerBuilder.newTrigger()
        .forJob(jobExecutionCleanupJobDetail)
        .withIdentity("job-execution-cleanup-trigger", "system-jobs")
        .withDescription("Run cleanup every 5 minutes")
        .withSchedule(CronScheduleBuilder.cronSchedule("0 */5 * * * ?"))
        .build();
  }
}
