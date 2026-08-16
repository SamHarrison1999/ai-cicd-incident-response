package com.samharrison.incidentresponse.learning;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.samharrison.incidentresponse.tenancy.CurrentUserProvider;
import com.samharrison.incidentresponse.tenancy.TenantAccessService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

class OperationalTrendDeepCoverageTest {
  private final UUID userId = UUID.randomUUID();
  private final UUID organisationId = UUID.randomUUID();
  private final UUID projectId = UUID.randomUUID();
  private final Instant start = Instant.parse("2026-01-01T00:00:00Z");
  private final Instant end = Instant.parse("2026-01-02T00:00:00Z");

  @Test
  void filtersAndComparesTrendWindows() {
    OperationalTrendRepository repository = mock(OperationalTrendRepository.class);
    TenantAccessService tenant = mock(TenantAccessService.class);
    OperationalTrend first = trend(UUID.randomUUID(), "service-a", 9, 4);
    OperationalTrend second = trend(UUID.randomUUID(), "service-a", 6, 2);
    when(repository
            .findAllByOrganisationIdAndProjectIdOrderByWindowEndDescDimensionAscDimensionKeyAscIdDesc(
                organisationId, projectId))
        .thenReturn(List.of(first, second));
    OperationalTrendService service = new OperationalTrendService(repository, tenant);

    TrendQueryCriteria criteria =
        new TrendQueryCriteria(TrendDimension.DIAGNOSIS_OUTCOME, "service-a", null, null, 50);
    assertThat(service.list(userId, organisationId, projectId, criteria))
        .containsExactly(first, second);
    TrendQueryCriteria boundedWindow =
        new TrendQueryCriteria(
            TrendDimension.DIAGNOSIS_OUTCOME,
            "service-a",
            start.minusSeconds(1),
            end.plusSeconds(1),
            50);
    assertThat(service.list(userId, organisationId, projectId, boundedWindow))
        .containsExactly(first, second);
    assertThat(
            service.list(
                userId,
                organisationId,
                projectId,
                new TrendQueryCriteria(null, null, null, null, 50)))
        .containsExactly(first, second);
    assertThat(
            service.list(
                userId,
                organisationId,
                projectId,
                new TrendQueryCriteria(
                    TrendDimension.DIAGNOSIS_OUTCOME, "service-a", end.plusSeconds(1), null, 50)))
        .isEmpty();
    assertThat(
            service.list(
                userId,
                organisationId,
                projectId,
                new TrendQueryCriteria(
                    TrendDimension.DIAGNOSIS_OUTCOME,
                    "service-a",
                    null,
                    start.minusSeconds(1),
                    50)))
        .isEmpty();
    OperationalTrendService.TrendComparison comparison =
        service.compare(userId, organisationId, projectId, criteria);
    assertThat(comparison.dimensionKey()).isEqualTo("service-a");
    assertThat(comparison.delta()).isEqualTo(2);

    TrendQueryCriteria mismatch =
        new TrendQueryCriteria(TrendDimension.DIAGNOSIS_OUTCOME, "other", null, null, 50);
    assertThat(service.compare(userId, organisationId, projectId, mismatch).suppressionReason())
        .isEqualTo(TrendSuppressionReason.INSUFFICIENT_SAMPLE);
  }

  @Test
  void controllerMapsTrendItemsAndCursor() {
    OperationalTrendService service = mock(OperationalTrendService.class);
    CurrentUserProvider users = mock(CurrentUserProvider.class);
    Authentication authentication = mock(Authentication.class);
    when(users.requireUserId(authentication)).thenReturn(userId);
    when(service.list(
            org.mockito.ArgumentMatchers.eq(userId),
            org.mockito.ArgumentMatchers.eq(organisationId),
            org.mockito.ArgumentMatchers.eq(projectId),
            org.mockito.ArgumentMatchers.any()))
        .thenReturn(List.of(trend(UUID.randomUUID(), "service-a", 3, 1)));
    OperationalTrendController controller = new OperationalTrendController(service, users);

    OperationalTrendController.TrendResponse response =
        controller.list(authentication, organisationId, projectId, null, null, null, null, 50);
    assertThat(response.items()).hasSize(1);
    assertThat(response.nextCursor()).isNotBlank();

    when(service.list(
            org.mockito.ArgumentMatchers.eq(userId),
            org.mockito.ArgumentMatchers.eq(organisationId),
            org.mockito.ArgumentMatchers.eq(projectId),
            org.mockito.ArgumentMatchers.any()))
        .thenReturn(List.of());
    OperationalTrendController.TrendResponse empty =
        controller.list(authentication, organisationId, projectId, null, null, null, null, 50);
    assertThat(empty.items()).isEmpty();
    assertThat(empty.nextCursor()).isNull();
  }

  @Test
  void excludesTrendsWithADifferentDimension() {
    OperationalTrendRepository repository = mock(OperationalTrendRepository.class);
    TenantAccessService tenant = mock(TenantAccessService.class);
    OperationalTrend matching = trend(UUID.randomUUID(), "service-a", 9, 4);
    OperationalTrend other =
        new OperationalTrend(
            UUID.randomUUID(),
            organisationId,
            projectId,
            TrendDimension.RECOMMENDATION_OUTCOME,
            "service-a",
            new ObservationWindow(start, end),
            "v1",
            9,
            4,
            "feedback",
            TrendSuppressionReason.NONE);
    when(repository
            .findAllByOrganisationIdAndProjectIdOrderByWindowEndDescDimensionAscDimensionKeyAscIdDesc(
                organisationId, projectId))
        .thenReturn(List.of(matching, other));

    assertThat(
            new OperationalTrendService(repository, tenant)
                .list(
                    userId,
                    organisationId,
                    projectId,
                    new TrendQueryCriteria(
                        TrendDimension.DIAGNOSIS_OUTCOME, "service-a", null, null, 50)))
        .containsExactly(matching);
  }

  private OperationalTrend trend(UUID id, String key, int sampleCount, int observedCount) {
    return new OperationalTrend(
        id,
        organisationId,
        projectId,
        TrendDimension.DIAGNOSIS_OUTCOME,
        key,
        new ObservationWindow(start, end),
        "v1",
        sampleCount,
        observedCount,
        "feedback",
        TrendSuppressionReason.NONE);
  }
}
