package com.samharrison.incidentresponse.ingestion;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;

class WebhookSecretPropertiesCoverageTest {
  @Test
  void normalisesNullAndCopiesConfiguredSecrets() {
    assertThat(new WebhookSecretProperties(null).secrets()).isEmpty();
    Map<String, String> configured = Map.of("github", "secret");
    assertThat(new WebhookSecretProperties(configured).secrets()).containsEntry("github", "secret");
    ConfiguredWebhookSecretResolver resolver =
        new ConfiguredWebhookSecretResolver(
            new WebhookSecretProperties(Map.of("github", "secret", "blank", " ")));
    assertThat(resolver.resolve("github")).isPresent();
    assertThat(resolver.resolve("blank")).isEmpty();
    assertThat(resolver.resolve("missing")).isEmpty();
  }
}
