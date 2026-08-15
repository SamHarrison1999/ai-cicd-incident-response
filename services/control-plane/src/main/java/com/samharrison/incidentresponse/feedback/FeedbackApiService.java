package com.samharrison.incidentresponse.feedback;

import com.samharrison.incidentresponse.tenancy.TenantAccessService;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class FeedbackApiService {
  private final FeedbackAggregateRepository repository;
  private final TenantAccessService tenantAccessService;

  public FeedbackApiService(
      FeedbackAggregateRepository repository, TenantAccessService tenantAccessService) {
    this.repository = repository;
    this.tenantAccessService = tenantAccessService;
  }

  public List<FeedbackAggregate> list(
      UUID userId, UUID organisationId, UUID projectId, FeedbackQueryCriteria criteria) {
    tenantAccessService.requireActiveMembership(organisationId, userId);
    return repository
        .findAllByOrganisationIdAndProjectIdOrderByWindowEndDescIdDesc(organisationId, projectId)
        .stream()
        .filter(
            item ->
                criteria.policyVersion() == null
                    || criteria.policyVersion().equals(item.getPolicyVersion()))
        .filter(item -> criteria.from() == null || !item.getWindowEnd().isBefore(criteria.from()))
        .filter(item -> criteria.to() == null || !item.getWindowStart().isAfter(criteria.to()))
        .limit(criteria.limit())
        .toList();
  }
}
