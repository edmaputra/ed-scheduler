package io.github.edmaputra.scheduler.application.port.out;

import io.github.edmaputra.scheduler.application.service.ScheduleHandle;
import io.github.edmaputra.scheduler.domain.Job;

public interface JobSchedulerPort {

  ScheduleHandle scheduleCron(Job job);

  ScheduleHandle scheduleDelayed(Job job);

  boolean interrupt(Job job);

  void unschedule(Job job);
}
