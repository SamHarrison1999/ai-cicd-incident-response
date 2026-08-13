package com.samharrison.incidentresponse.ingestion;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(WebhookSecretProperties.class)
public class WebhookIngestionConfiguration {

  @Bean
  WebhookSecretResolver webhookSecretResolver(WebhookSecretProperties properties) {
    return new ConfiguredWebhookSecretResolver(properties);
  }
}
