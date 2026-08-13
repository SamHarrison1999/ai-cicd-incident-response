package com.samharrison.incidentresponse.ingestion;

import java.util.Optional;

public interface WebhookSecretResolver {

  Optional<byte[]> resolve(String reference);
}
