package com.samharrison.incidentresponse.learning;

import com.samharrison.incidentresponse.feedback.FeedbackMaterializationService;
import com.samharrison.incidentresponse.feedback.FeedbackOutcome;
import com.samharrison.incidentresponse.feedback.FeedbackSignal;
import com.samharrison.incidentresponse.feedback.FeedbackSignalRepository;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class OperationalLearningMaterializationService {
  public static final String AGGREGATION_VERSION = "governed-feedback-daily-v1";

  private static final TrendDimension DIMENSION = TrendDimension.RECOMMENDATION_OUTCOME;

  private final FeedbackSignalRepository signalRepository;
  private final OperationalTrendRepository trendRepository;
  private final DeterministicTrendObservationService observationService;

  public OperationalLearningMaterializationService(
      FeedbackSignalRepository signalRepository,
      OperationalTrendRepository trendRepository,
      DeterministicTrendObservationService observationService) {
    this.signalRepository = signalRepository;
    this.trendRepository = trendRepository;
    this.observationService = observationService;
  }

  public List<OperationalTrend> materialize(UUID organisationId, UUID projectId) {
    Objects.requireNonNull(organisationId);
    Objects.requireNonNull(projectId);

    List<FeedbackSignal> governedSignals =
        signalRepository
            .findAllByOrganisationIdAndProjectIdOrderByCreatedAtAscIdAsc(organisationId, projectId)
            .stream()
            .filter(
                signal ->
                    FeedbackMaterializationService.POLICY_VERSION.equals(signal.getPolicyVersion()))
            .sorted(
                Comparator.comparing(FeedbackSignal::getCreatedAt)
                    .thenComparing(FeedbackSignal::getId))
            .toList();

    if (governedSignals.isEmpty()) {
      return List.of();
    }

    List<TrendObservation> observations =
        governedSignals.stream()
            .map(
                signal ->
                    new TrendObservation(
                        organisationId,
                        projectId,
                        DIMENSION,
                        signal.getOutcome().name(),
                        signal.getCreatedAt(),
                        sourceReference(signal)))
            .toList();

    Map<LocalDate, List<FeedbackSignal>> signalsByDay =
        governedSignals.stream()
            .collect(
                Collectors.groupingBy(
                    signal -> signal.getCreatedAt().atZone(ZoneOffset.UTC).toLocalDate(),
                    TreeMap::new,
                    Collectors.toList()));

    List<OperationalTrend> persisted = new ArrayList<>();

    for (Map.Entry<LocalDate, List<FeedbackSignal>> entry : signalsByDay.entrySet()) {
      List<FeedbackSignal> dailySignals =
          entry.getValue().stream()
              .sorted(
                  Comparator.comparing(FeedbackSignal::getCreatedAt)
                      .thenComparing(FeedbackSignal::getId))
              .toList();

      ObservationWindow window =
          new ObservationWindow(
              dailySignals.get(0).getCreatedAt(),
              dailySignals.get(dailySignals.size() - 1).getCreatedAt());

      List<FeedbackOutcome> outcomes =
          dailySignals.stream()
              .map(FeedbackSignal::getOutcome)
              .distinct()
              .sorted(Comparator.comparing(FeedbackOutcome::name))
              .toList();

      for (FeedbackOutcome outcome : outcomes) {
        OperationalTrend calculated =
            observationService.observe(
                organisationId,
                projectId,
                DIMENSION,
                outcome.name(),
                window,
                AGGREGATION_VERSION,
                observations);

        UUID trendId =
            deterministicId(
                "operational-trend",
                organisationId.toString(),
                projectId.toString(),
                DIMENSION.name(),
                outcome.name(),
                entry.getKey().toString(),
                AGGREGATION_VERSION);

        OperationalTrend persistedTrend =
            trendRepository.save(
                new OperationalTrend(
                    trendId,
                    organisationId,
                    projectId,
                    DIMENSION,
                    outcome.name(),
                    window,
                    AGGREGATION_VERSION,
                    calculated.getSampleCount(),
                    calculated.getObservedCount(),
                    calculated.getSourceReference(),
                    calculated.getSuppressionReason()));

        persisted.add(persistedTrend);
      }
    }

    return List.copyOf(persisted);
  }

  private static String sourceReference(FeedbackSignal signal) {
    return "feedback-signal:" + signal.getId();
  }

  private static UUID deterministicId(String... parts) {
    return UUID.nameUUIDFromBytes(String.join(":", parts).getBytes(StandardCharsets.UTF_8));
  }
}
