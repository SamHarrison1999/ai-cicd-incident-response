package com.samharrison.incidentresponse.ingestion;

import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "application.webhooks")
public record WebhookSecretProperties(Map<String, String> secrets) {

  public WebhookSecretProperties {
    secrets = secrets == null ? Map.of() : Map.copyOf(secrets);
  }
}
