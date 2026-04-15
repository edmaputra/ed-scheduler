package io.github.edmaputra.scheduler.adapter.in.quartz;

import io.github.edmaputra.scheduler.application.port.in.ScheduledJobExecutionUseCase;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

/**
 * Quartz job implementation that executes scheduled jobs This job is triggered by Quartz scheduler
 * when a job's time arrives
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledJob extends QuartzJobBean {

  private final ScheduledJobExecutionUseCase scheduledJobExecutionUseCase;

  @Override
  protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
    JobDataMap dataMap = context.getJobDetail().getJobDataMap();
    String jobIdStr = dataMap.getString("jobId");
    UUID jobId = UUID.fromString(jobIdStr);
    try {
      scheduledJobExecutionUseCase.execute(jobId);
    } catch (Exception e) {
      log.error("Job {} failed with error: {}", jobId, e.getMessage(), e);
      throw new JobExecutionException(e);
    }
  }
}
