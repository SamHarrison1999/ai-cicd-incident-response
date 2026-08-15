package com.samharrison.incidentresponse.learning;

import com.samharrison.incidentresponse.tenancy.CurrentUserProvider;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/organisations/{organisationId}/projects/{projectId}/operational-learning")
public class OperationalTrendController {
  private final OperationalTrendService service;
  private final CurrentUserProvider currentUserProvider;

  public OperationalTrendController(
      OperationalTrendService service, CurrentUserProvider currentUserProvider) {
    this.service = service;
    this.currentUserProvider = currentUserProvider;
  }

  @GetMapping("/trends")
  TrendResponse list(
      Authentication authentication,
      @PathVariable UUID organisationId,
      @PathVariable UUID projectId,
      @RequestParam(required = false) TrendDimension dimension,
      @RequestParam(required = false) String dimensionKey,
      @RequestParam(required = false) Instant from,
      @RequestParam(required = false) Instant to,
      @RequestParam(defaultValue = "50") int limit) {
    UUID userId = currentUserProvider.requireUserId(authentication);
    TrendQueryCriteria criteria = new TrendQueryCriteria(dimension, dimensionKey, from, to, limit);
    List<TrendItem> items =
        service.list(userId, organisationId, projectId, criteria).stream()
            .map(TrendItem::from)
            .toList();
    String nextCursor =
        items.isEmpty()
            ? null
            : new TrendCursor(
                    items.get(items.size() - 1).windowEnd(), items.get(items.size() - 1).id())
                .encode();
    return new TrendResponse(items, nextCursor, false);
  }

  @GetMapping("/trends/compare")
  TrendComparisonResponse compare(
      Authentication authentication,
      @PathVariable UUID organisationId,
      @PathVariable UUID projectId,
      @RequestParam(required = false) TrendDimension dimension,
      @RequestParam(required = false) String dimensionKey,
      @RequestParam(required = false) Instant from,
      @RequestParam(required = false) Instant to,
      @RequestParam(defaultValue = "50") int limit) {
    UUID userId = currentUserProvider.requireUserId(authentication);
    TrendQueryCriteria criteria = new TrendQueryCriteria(dimension, dimensionKey, from, to, limit);
    return TrendComparisonResponse.from(
        service.compare(userId, organisationId, projectId, criteria));
  }

  public record TrendResponse(List<TrendItem> items, String nextCursor, boolean hasNext) {}

  public record TrendItem(
      UUID id,
      TrendDimension dimension,
      String dimensionKey,
      Instant windowStart,
      Instant windowEnd,
      String aggregationVersion,
      int sampleCount,
      int observedCount,
      String sourceReference,
      TrendSuppressionReason suppressionReason) {
    static TrendItem from(OperationalTrend item) {
      return new TrendItem(
          item.getId(),
          item.getDimension(),
          item.getDimensionKey(),
          item.getWindowStart(),
          item.getWindowEnd(),
          item.getAggregationVersion(),
          item.getSampleCount(),
          item.getObservedCount(),
          item.getSourceReference(),
          item.getSuppressionReason());
    }
  }

  public record TrendComparisonResponse(
      String dimensionKey,
      int currentCount,
      int previousCount,
      int delta,
      TrendSuppressionReason suppressionReason) {
    static TrendComparisonResponse from(OperationalTrendService.TrendComparison comparison) {
      return new TrendComparisonResponse(
          comparison.dimensionKey(),
          comparison.currentCount(),
          comparison.previousCount(),
          comparison.delta(),
          comparison.suppressionReason());
    }
  }
}
