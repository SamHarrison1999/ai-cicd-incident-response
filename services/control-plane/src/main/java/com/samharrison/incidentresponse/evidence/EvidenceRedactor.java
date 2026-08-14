package com.samharrison.incidentresponse.evidence;

import java.util.regex.Pattern;

public final class EvidenceRedactor {

  public static final int MAX_CHARS = 12_000;
  public static final int MAX_LINES = 200;
  private static final Pattern SECRET =
      Pattern.compile("(?i)\\b(password|secret|token|api[-_]?key)\\s*[:=]\\s*([^\\s,;]+)");
  private static final Pattern BEARER = Pattern.compile("(?i)\\bBearer\\s+[A-Za-z0-9._~+/=-]+");
  private static final Pattern SIGNATURE = Pattern.compile("(?i)\\bsha256=[a-f0-9]{32,}");

  private EvidenceRedactor() {}

  public static RedactedContent redact(String rawContent) {
    if (rawContent == null || rawContent.isBlank()) {
      throw new IllegalArgumentException("content must not be blank");
    }
    String normalised = rawContent.replace("\r\n", "\n").replace('\r', '\n');
    String redacted = SECRET.matcher(normalised).replaceAll("$1=[REDACTED]");
    redacted = BEARER.matcher(redacted).replaceAll("Bearer [REDACTED]");
    redacted = SIGNATURE.matcher(redacted).replaceAll("sha256=[REDACTED]");
    String[] lines = redacted.split("\\n", -1);
    if (lines.length > MAX_LINES) {
      StringBuilder bounded = new StringBuilder();
      for (int index = 0; index < MAX_LINES; index++) {
        if (index > 0) {
          bounded.append('\n');
        }
        bounded.append(lines[index]);
      }
      redacted = bounded.append("\n[TRUNCATED]").toString();
    }
    if (redacted.length() > MAX_CHARS) {
      redacted = redacted.substring(0, MAX_CHARS - 12) + "\n[TRUNCATED]";
    }
    return new RedactedContent(redacted, redacted.split("\\n", -1).length);
  }

  public record RedactedContent(String content, int lineCount) {}
}
