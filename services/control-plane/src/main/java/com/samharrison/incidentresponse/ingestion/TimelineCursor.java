package com.samharrison.incidentresponse.ingestion;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

public record TimelineCursor(Instant occurredAt, Instant receivedAt, UUID eventId) {

  public String encode() {
    String value = occurredAt + "|" + receivedAt + "|" + eventId;
    return Base64.getUrlEncoder()
        .withoutPadding()
        .encodeToString(value.getBytes(StandardCharsets.UTF_8));
  }

  public static TimelineCursor decode(String encoded) {
    if (encoded == null || encoded.isBlank()) {
      throw new IllegalArgumentException("cursor must not be blank");
    }
    try {
      String value = new String(Base64.getUrlDecoder().decode(encoded), StandardCharsets.UTF_8);
      String[] parts = value.split("\\|", -1);
      if (parts.length != 3) {
        throw new IllegalArgumentException("cursor has an invalid shape");
      }
      return new TimelineCursor(
          Instant.parse(parts[0]), Instant.parse(parts[1]), UUID.fromString(parts[2]));
    } catch (RuntimeException exception) {
      throw new IllegalArgumentException("cursor is invalid", exception);
    }
  }
}
