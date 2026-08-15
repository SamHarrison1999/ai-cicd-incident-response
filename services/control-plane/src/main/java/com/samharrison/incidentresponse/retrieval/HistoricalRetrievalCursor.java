package com.samharrison.incidentresponse.retrieval;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

public record HistoricalRetrievalCursor(Instant occurredAt, UUID id) {

  public String encode() {
    String value = occurredAt.toString() + "|" + id;
    return Base64.getUrlEncoder()
        .withoutPadding()
        .encodeToString(value.getBytes(StandardCharsets.UTF_8));
  }

  public static HistoricalRetrievalCursor decode(String encoded) {
    if (encoded == null || encoded.isBlank()) {
      return null;
    }
    try {
      String value = new String(Base64.getUrlDecoder().decode(encoded), StandardCharsets.UTF_8);
      String[] parts = value.split("\\|", 2);
      if (parts.length != 2) {
        throw new IllegalArgumentException("cursor shape");
      }
      return new HistoricalRetrievalCursor(Instant.parse(parts[0]), UUID.fromString(parts[1]));
    } catch (RuntimeException exception) {
      throw new IllegalArgumentException("cursor is invalid", exception);
    }
  }
}
