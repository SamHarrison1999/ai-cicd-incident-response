package com.samharrison.incidentresponse.feedback;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.samharrison.incidentresponse.tenancy.TenantAccessService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class FeedbackApiCoverageTest {

  @Test
  void filtersFeedbackByPolicyAndOverlappingWindowAndAppliesLimit() {
    FeedbackAggregateRepository repository = mock(FeedbackAggregateRepository.class);
    TenantAccessService tenant = mock(TenantAccessService.class);
    UUID organisationId = UUID.randomUUID();
    UUID projectId = UUID.randomUUID();
    Instant start = Instant.parse("2026-08-15T00:00:00Z");
    Instant end = Instant.parse("2026-08-16T00:00:00Z");
    FeedbackAggregate matching = aggregate(organisationId, projectId, "v1", start, end);
    FeedbackAggregate wrongPolicy = aggregate(organisationId, projectId, "v2", start, end);
    FeedbackAggregate outside =
        aggregate(organisationId, projectId, "v1", end.plusSeconds(1), end.plusSeconds(2));
    when(repository.findAllByOrganisationIdAndProjectIdOrderByWindowEndDescIdDesc(
            organisationId, projectId))
        .thenReturn(List.of(matching, wrongPolicy, outside));

    FeedbackApiService service = new FeedbackApiService(repository, tenant);
    assertThat(
            service.list(
                UUID.randomUUID(),
                organisationId,
                projectId,
                new FeedbackQueryCriteria("v1", start, end, 1)))
        .containsExactly(matching);
    assertThat(
            service.list(
                UUID.randomUUID(), organisationId, projectId, FeedbackQueryCriteria.defaults()))
        .containsExactly(matching, wrongPolicy, outside);
    assertThat(
            service.list(
                UUID.randomUUID(),
                organisationId,
                projectId,
                new FeedbackQueryCriteria(null, start, end, 50)))
        .containsExactly(matching, wrongPolicy);
    assertThat(
            service.list(
                UUID.randomUUID(),
                organisationId,
                projectId,
                new FeedbackQueryCriteria("v1", null, null, 50)))
        .containsExactly(matching, outside);

    FeedbackAggregate before =
        aggregate(organisationId, projectId, "v1", start.minusSeconds(2), start.minusSeconds(1));
    when(repository.findAllByOrganisationIdAndProjectIdOrderByWindowEndDescIdDesc(
            organisationId, projectId))
        .thenReturn(List.of(before, matching));
    assertThat(
            service.list(
                UUID.randomUUID(),
                organisationId,
                projectId,
                new FeedbackQueryCriteria("v1", start, end, 50)))
        .containsExactly(matching);
  }

  private static FeedbackAggregate aggregate(
      UUID organisationId, UUID projectId, String policy, Instant start, Instant end) {
    return new FeedbackAggregate(
        UUID.randomUUID(),
        organisationId,
        projectId,
        policy,
        start,
        end,
        3,
        1,
        1,
        1,
        0,
        FeedbackSuppressionReason.NONE);
  }
}
