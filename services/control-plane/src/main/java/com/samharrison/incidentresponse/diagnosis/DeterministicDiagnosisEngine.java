package com.samharrison.incidentresponse.diagnosis;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public final class DeterministicDiagnosisEngine {

  public static final String RULE_VERSION = "phase-7-diagnosis-rules-1";
  public static final int MAX_SIGNALS = 100;

  private static final Comparator<DiagnosisSignal> SIGNAL_ORDER =
      Comparator.comparing(DiagnosisSignal::occurredAt)
          .reversed()
          .thenComparing(DiagnosisSignal::id, Comparator.reverseOrder());

  private static final Map<DiagnosisCategory, List<Pattern>> RULES = rules();

  public DiagnosisResult diagnose(List<DiagnosisSignal> input) {
    if (input == null) {
      throw new IllegalArgumentException("signals must not be null");
    }
    if (input.size() > MAX_SIGNALS) {
      throw new IllegalArgumentException("signals exceed the diagnosis bound");
    }

    List<DiagnosisSignal> signals = new ArrayList<>(input);
    signals.sort(SIGNAL_ORDER);
    if (signals.isEmpty()) {
      return new DiagnosisResult(
          RULE_VERSION,
          DiagnosisCategory.INSUFFICIENT_EVIDENCE,
          0.0,
          List.of(),
          List.of("sanitised technical signals"),
          List.of(),
          "NO_SANITISED_SIGNALS");
    }

    EnumMap<DiagnosisCategory, Integer> scores = new EnumMap<>(DiagnosisCategory.class);
    EnumMap<DiagnosisCategory, List<UUID>> matchingSignals = new EnumMap<>(DiagnosisCategory.class);
    List<String> warnings = new ArrayList<>();
    for (DiagnosisSignal signal : signals) {
      String text = signal.sanitisedText().toLowerCase(Locale.ROOT);
      if (text.contains("[untrusted_instruction_removed]")) {
        warnings.add("UNTRUSTED_INSTRUCTION_REMOVED");
      }
      for (Map.Entry<DiagnosisCategory, List<Pattern>> rule : RULES.entrySet()) {
        if (rule.getValue().stream().anyMatch(pattern -> pattern.matcher(text).find())) {
          scores.merge(rule.getKey(), 1, Integer::sum);
          matchingSignals
              .computeIfAbsent(rule.getKey(), ignored -> new ArrayList<>())
              .add(signal.id());
        }
      }
    }

    if (scores.isEmpty()) {
      return new DiagnosisResult(
          RULE_VERSION,
          DiagnosisCategory.UNKNOWN,
          0.2,
          List.of(),
          List.of("a signal matching a supported diagnosis rule"),
          distinct(warnings),
          "NO_SUPPORTED_RULE_MATCH");
    }

    int highestScore = scores.values().stream().mapToInt(Integer::intValue).max().orElse(0);
    List<DiagnosisCategory> winners =
        scores.entrySet().stream()
            .filter(entry -> entry.getValue() == highestScore)
            .map(Map.Entry::getKey)
            .sorted()
            .toList();
    if (winners.size() != 1) {
      return new DiagnosisResult(
          RULE_VERSION,
          DiagnosisCategory.UNKNOWN,
          0.35,
          List.of(),
          List.of("a uniquely supported diagnosis category"),
          distinct(warnings),
          "AMBIGUOUS_RULE_MATCH");
    }

    DiagnosisCategory category = winners.getFirst();
    List<UUID> supportingIds =
        matchingSignals.getOrDefault(category, List.of()).stream().distinct().toList();
    double confidence =
        Math.min(
            0.95, 0.55 + (highestScore * 0.1) + (Math.min(3, supportingIds.size() - 1) * 0.05));
    return new DiagnosisResult(
        RULE_VERSION, category, confidence, supportingIds, List.of(), distinct(warnings), null);
  }

  private static List<String> distinct(List<String> values) {
    return values.stream().distinct().sorted().toList();
  }

  private static Map<DiagnosisCategory, List<Pattern>> rules() {
    EnumMap<DiagnosisCategory, List<Pattern>> rules = new EnumMap<>(DiagnosisCategory.class);
    rules.put(
        DiagnosisCategory.DEPENDENCY_FAILURE_SUSPECTED,
        patterns("timeout", "connection refused", "upstream unavailable", "dependency failed"));
    rules.put(
        DiagnosisCategory.DEPLOYMENT_REGRESSION_SUSPECTED,
        patterns("deployment failed", "release failed", "rollback", "new version"));
    rules.put(
        DiagnosisCategory.CONFIGURATION_CHANGE_SUSPECTED,
        patterns(
            "configuration changed", "config changed", "feature flag", "environment variable"));
    rules.put(
        DiagnosisCategory.RESOURCE_EXHAUSTION_SUSPECTED,
        patterns("out of memory", "oom", "cpu limit", "memory limit", "disk full"));
    return Map.copyOf(rules);
  }

  private static List<Pattern> patterns(String... values) {
    return java.util.Arrays.stream(values).map(Pattern::compile).toList();
  }
}
