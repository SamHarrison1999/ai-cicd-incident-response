package com.samharrison.incidentresponse.learning;

import com.samharrison.incidentresponse.tenancy.TenantAccessService;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class OperationalTrendService {
  private final OperationalTrendRepository repository;
  private final TenantAccessService tenantAccessService;

  public OperationalTrendService(
      OperationalTrendRepository repository, TenantAccessService tenantAccessService) {
    this.repository = repository;
    this.tenantAccessService = tenantAccessService;
  }

  public List<OperationalTrend> list(
      UUID userId, UUID organisationId, UUID projectId, TrendQueryCriteria criteria) {
    tenantAccessService.requireActiveMembership(organisationId, userId);
    return repository
        .findAllByOrganisationIdAndProjectIdOrderByWindowEndDescDimensionAscDimensionKeyAscIdDesc(
            organisationId, projectId)
        .stream()
        .filter(item -> criteria.dimension() == null || criteria.dimension() == item.getDimension())
        .filter(
            item ->
                criteria.dimensionKey() == null
                    || criteria.dimensionKey().equals(item.getDimensionKey()))
        .filter(item -> criteria.from() == null || !item.getWindowEnd().isBefore(criteria.from()))
        .filter(item -> criteria.to() == null || !item.getWindowStart().isAfter(criteria.to()))
        .limit(criteria.limit())
        .toList();
  }

  public TrendComparison compare(
      UUID userId, UUID organisationId, UUID projectId, TrendQueryCriteria criteria) {
    List<OperationalTrend> items = list(userId, organisationId, projectId, criteria);

    if (items.isEmpty()) {
      return new TrendComparison(null, 0, 0, 0, TrendSuppressionReason.INSUFFICIENT_SAMPLE);
    }

    OperationalTrend current = items.get(0);

    OperationalTrend previous =
        items.stream()
            .skip(1)
            .filter(item -> current.getDimension() == item.getDimension())
            .filter(item -> current.getDimensionKey().equals(item.getDimensionKey()))
            .findFirst()
            .orElse(null);

    if (previous == null) {
      return new TrendComparison(null, 0, 0, 0, TrendSuppressionReason.INSUFFICIENT_SAMPLE);
    }

    return new TrendComparison(
        current.getDimensionKey(),
        current.getObservedCount(),
        previous.getObservedCount(),
        current.getObservedCount() - previous.getObservedCount(),
        current.getSuppressionReason());
  }

  public record TrendComparison(
      String dimensionKey,
      int currentCount,
      int previousCount,
      int delta,
      TrendSuppressionReason suppressionReason) {}
}
