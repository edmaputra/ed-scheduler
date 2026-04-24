package io.github.edmaputra.scheduler;

import static org.mockito.Mockito.mock;

import io.github.edmaputra.scheduler.adapter.out.messaging.MessagePublisher;
import org.junit.jupiter.api.Test;
import org.quartz.Scheduler;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = {SchedulerApplication.class, SchedulerApplicationTests.ContextTestConfig.class})
@ActiveProfiles("test")
class SchedulerApplicationTests {

  @TestConfiguration
  static class ContextTestConfig {

    @Bean
    @Primary
    Scheduler testScheduler() {
      return mock(Scheduler.class);
    }

    @Bean
    @Primary
    MessagePublisher testMessagePublisher() {
      return mock(MessagePublisher.class);
    }
  }

  @Test
  void contextLoads() {
  }

}
