package io.github.edmaputra.scheduler.adapter.out.scheduler;

import io.github.edmaputra.scheduler.adapter.in.quartz.ScheduledJob;
import io.github.edmaputra.scheduler.application.port.out.JobSchedulerPort;
import io.github.edmaputra.scheduler.application.service.ScheduleHandle;
import io.github.edmaputra.scheduler.domain.Job;
import java.time.ZoneId;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuartzJobSchedulerAdapter implements JobSchedulerPort {

  private final Scheduler scheduler;

  @Override
  public ScheduleHandle scheduleCron(Job job) {
    try {
      String jobKey = "job-" + job.getId();
      String triggerKey = "trigger-" + job.getId();

      JobDetail jobDetail = JobBuilder.newJob(ScheduledJob.class)
          .withIdentity(jobKey)
          .usingJobData("jobId", job.getId().toString())
          .build();

      CronTrigger trigger = TriggerBuilder.newTrigger()
          .withIdentity(triggerKey)
          .withSchedule(CronScheduleBuilder.cronSchedule(job.getCronExpression()))
          .build();

      scheduler.scheduleJob(jobDetail, trigger);
      return new ScheduleHandle(jobKey, triggerKey);
    } catch (SchedulerException e) {
      log.error("Failed to schedule CRON job in Quartz", e);
      throw new RuntimeException("Failed to schedule CRON job", e);
    }
  }

  @Override
  public ScheduleHandle scheduleDelayed(Job job) {
    try {
      String jobKey = "job-" + job.getId();
      String triggerKey = "trigger-" + job.getId();

      JobDetail jobDetail = JobBuilder.newJob(ScheduledJob.class)
          .withIdentity(jobKey)
          .usingJobData("jobId", job.getId().toString())
          .build();

      Date scheduledDate = Date.from(job.getScheduledTime()
          .atZone(ZoneId.systemDefault())
          .toInstant());

      Trigger trigger = TriggerBuilder.newTrigger()
          .withIdentity(triggerKey)
          .startAt(scheduledDate)
          .build();

      scheduler.scheduleJob(jobDetail, trigger);
      return new ScheduleHandle(jobKey, triggerKey);
    } catch (SchedulerException e) {
      log.error("Failed to schedule delayed job in Quartz", e);
      throw new RuntimeException("Failed to schedule delayed job", e);
    }
  }

  @Override
  public boolean interrupt(Job job) {
    try {
      if (job.getQuartzJobKey() == null || job.getQuartzJobKey().isEmpty()) {
        return false;
      }

      return scheduler.interrupt(new JobKey(job.getQuartzJobKey()));
    } catch (SchedulerException e) {
      log.error("Failed to interrupt job in Quartz", e);
      throw new RuntimeException("Failed to interrupt job", e);
    }
  }

  @Override
  public void unschedule(Job job) {
    try {
      if (job.getQuartzTriggerKey() != null && !job.getQuartzTriggerKey().isEmpty()) {
        scheduler.unscheduleJob(new TriggerKey(job.getQuartzTriggerKey()));
      }
      if (job.getQuartzJobKey() != null && !job.getQuartzJobKey().isEmpty()) {
        scheduler.deleteJob(new JobKey(job.getQuartzJobKey()));
      }
    } catch (SchedulerException e) {
      log.error("Failed to unschedule job from Quartz", e);
      throw new RuntimeException("Failed to unschedule job", e);
    }
  }
}
