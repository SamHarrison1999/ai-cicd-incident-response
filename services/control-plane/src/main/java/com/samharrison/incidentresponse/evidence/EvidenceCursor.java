package com.samharrison.incidentresponse.evidence;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

public record EvidenceCursor(Instant occurredAt, UUID id) {

  public EvidenceCursor {
    if (occurredAt == null || id == null) {
      throw new IllegalArgumentException("Evidence cursor values are required");
    }
  }

  public String encode() {
    String value = occurredAt + "|" + id;
    return Base64.getUrlEncoder()
        .withoutPadding()
        .encodeToString(value.getBytes(StandardCharsets.UTF_8));
  }

  public static EvidenceCursor decode(String encoded) {
    if (encoded == null || encoded.isBlank()) {
      return null;
    }
    try {
      String value = new String(Base64.getUrlDecoder().decode(encoded), StandardCharsets.UTF_8);
      String[] parts = value.split("\\|", -1);
      if (parts.length != 2) {
        throw new IllegalArgumentException("Evidence cursor must contain two values");
      }
      return new EvidenceCursor(Instant.parse(parts[0]), UUID.fromString(parts[1]));
    } catch (RuntimeException exception) {
      throw new IllegalArgumentException("Evidence cursor is malformed", exception);
    }
  }
}
