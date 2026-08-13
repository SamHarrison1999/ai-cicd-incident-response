package com.samharrison.incidentresponse.ingestion;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.util.Optional;

interface ProviderEventAdapter {

  EventProvider provider();

  Optional<NormalisedEventCandidate> adapt(
      String providerEventType, JsonNode payload, Instant fallbackOccurredAt);
}
