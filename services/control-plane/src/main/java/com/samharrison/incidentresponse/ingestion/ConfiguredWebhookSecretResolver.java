package com.samharrison.incidentresponse.ingestion;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

final class ConfiguredWebhookSecretResolver implements WebhookSecretResolver {

  private final WebhookSecretProperties properties;

  ConfiguredWebhookSecretResolver(WebhookSecretProperties properties) {
    this.properties = properties;
  }

  @Override
  public Optional<byte[]> resolve(String reference) {
    String secret = properties.secrets().get(reference);
    if (secret == null || secret.isBlank()) {
      return Optional.empty();
    }
    return Optional.of(secret.getBytes(StandardCharsets.UTF_8));
  }
}
