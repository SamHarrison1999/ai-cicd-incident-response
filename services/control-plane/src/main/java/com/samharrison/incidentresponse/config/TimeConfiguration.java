package com.samharrison.incidentresponse.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class TimeConfiguration {

  @Bean
  Clock systemClock() {
    return Clock.systemUTC();
  }
}
