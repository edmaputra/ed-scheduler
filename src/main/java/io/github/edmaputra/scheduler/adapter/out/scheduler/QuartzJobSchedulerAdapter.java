package io.github.edmaputra.scheduler.adapter.out.scheduler;

import io.github.edmaputra.scheduler.application.port.out.JobSchedulerPort;
import io.github.edmaputra.scheduler.application.service.ScheduleHandle;
import io.github.edmaputra.scheduler.domain.Job;
import io.github.edmaputra.scheduler.adapter.in.quartz.ScheduledJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.Date;

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
