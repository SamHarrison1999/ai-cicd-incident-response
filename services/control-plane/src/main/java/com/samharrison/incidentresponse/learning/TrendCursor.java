package com.samharrison.incidentresponse.learning;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

public record TrendCursor(Instant windowEnd, UUID id) {
  public String encode() {
    String value = windowEnd.toString() + "|" + id;
    return Base64.getUrlEncoder()
        .withoutPadding()
        .encodeToString(value.getBytes(StandardCharsets.UTF_8));
  }

  public static TrendCursor decode(String encoded) {
    try {
      String value = new String(Base64.getUrlDecoder().decode(encoded), StandardCharsets.UTF_8);
      String[] parts = value.split("\\|", -1);
      if (parts.length != 2) {
        throw new IllegalArgumentException("cursor is malformed");
      }
      return new TrendCursor(Instant.parse(parts[0]), UUID.fromString(parts[1]));
    } catch (RuntimeException exception) {
      throw new IllegalArgumentException("cursor is malformed", exception);
    }
  }
}
