package com.samharrison.incidentresponse.retrieval;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;

@ExtendWith(MockitoExtension.class)
class HistoricalRetrievalQueryServiceTest {

  @Mock private HistoricalRetrievalRecordRepository repository;

  @Test
  void forwardsTenantAndBoundedCriteriaToRepository() {
    UUID organisationId = UUID.randomUUID();
    UUID projectId = UUID.randomUUID();
    HistoricalRetrievalCriteria criteria =
        new HistoricalRetrievalCriteria(
            "DEPENDENCY_FAILURE_SUSPECTED",
            "GITHUB_ACTIONS",
            "checkout",
            "production",
            "refs/heads/main",
            null,
            Instant.parse("2026-08-01T00:00:00Z"),
            null,
            "timeout",
            25);
    when(repository.search(
            eq(organisationId),
            eq(projectId),
            eq(criteria.diagnosisCategory()),
            eq(criteria.provider()),
            eq(criteria.pipelineName()),
            eq(criteria.environmentName()),
            eq(criteria.gitRef()),
            eq(criteria.commitSha()),
            eq(criteria.query()),
            eq(criteria.occurredFrom()),
            eq(criteria.occurredTo()),
            any(),
            any(),
            any()))
        .thenReturn(new PageImpl<>(java.util.List.of()));

    assertThat(
            new HistoricalRetrievalQueryService(repository)
                .search(organisationId, projectId, criteria, null, null)
                .getContent())
        .isEmpty();
    verify(repository)
        .search(
            eq(organisationId),
            eq(projectId),
            eq("DEPENDENCY_FAILURE_SUSPECTED"),
            eq("GITHUB_ACTIONS"),
            eq("checkout"),
            eq("production"),
            eq("refs/heads/main"),
            eq(null),
            eq("timeout"),
            eq(Instant.parse("2026-08-01T00:00:00Z")),
            eq(null),
            eq(null),
            eq(null),
            any());
  }
}
