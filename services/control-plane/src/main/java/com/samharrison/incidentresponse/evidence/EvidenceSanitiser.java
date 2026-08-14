package com.samharrison.incidentresponse.evidence;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public final class EvidenceSanitiser {

  public static final String VERSION = "phase-7-sanitiser-1";
  private static final String REMOVED_INSTRUCTION = "[UNTRUSTED_INSTRUCTION_REMOVED]";
  private static final Pattern INSTRUCTION_PATTERN =
      Pattern.compile(
          "(?i)(\\b(ignore|disregard|override)\\s+(all|any|the|these|previous)\\s+(instructions?|rules?|messages?)\\b|"
              + "\\b(system|developer)\\s+(prompt|message)\\b|"
              + "\\b(reveal|print|exfiltrate|show)\\s+(the\\s+)?(secret|token|password|api[-_ ]?key)\\b)");

  private EvidenceSanitiser() {}

  public static SanitisedEvidence sanitise(String rawContent) {
    EvidenceRedactor.RedactedContent redacted = EvidenceRedactor.redact(rawContent);
    List<SanitisationWarning> warnings = new ArrayList<>();
    String normalised = rawContent.replace("\r\n", "\n").replace('\r', '\n');
    if (!normalised.equals(redacted.content())) {
      warnings.add(SanitisationWarning.SECRET_REDACTED);
    }
    if (redacted.content().contains("[TRUNCATED]")) {
      warnings.add(SanitisationWarning.CONTENT_BOUNDED);
    }

    String[] lines = redacted.content().split("\\n", -1);
    StringBuilder sanitised = new StringBuilder(redacted.content().length());
    for (int index = 0; index < lines.length; index++) {
      if (index > 0) {
        sanitised.append('\n');
      }
      String line = lines[index];
      if (INSTRUCTION_PATTERN.matcher(line).find()) {
        sanitised.append(REMOVED_INSTRUCTION);
        if (!warnings.contains(SanitisationWarning.PROMPT_INJECTION_REMOVED)) {
          warnings.add(SanitisationWarning.PROMPT_INJECTION_REMOVED);
        }
      } else {
        sanitised.append(line);
      }
    }

    String result = sanitised.toString();
    return new SanitisedEvidence(VERSION, result, result.split("\\n", -1).length, warnings);
  }
}
