package io.github.edmaputra.scheduler.adapter.out.messaging;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessagePublisherConfiguration {

  @Bean
  @ConditionalOnMissingBean(MessagePublisher.class)
  public MessagePublisher messagePublisher() {
    return new DefaultMessagePublisher();
  }
}
