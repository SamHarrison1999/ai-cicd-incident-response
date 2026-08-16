package com.samharrison.incidentresponse.evidence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.samharrison.incidentresponse.tenancy.CurrentUserProvider;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

class EvidenceControllerCoverageTest {
  @Test
  void mapsSearchResultsAndCursor() {
    EvidenceSearchService search = mock(EvidenceSearchService.class);
    CurrentUserProvider users = mock(CurrentUserProvider.class);
    Authentication authentication = mock(Authentication.class);
    UUID userId = UUID.randomUUID();
    UUID organisationId = UUID.randomUUID();
    UUID projectId = UUID.randomUUID();
    Evidence evidence = mock(Evidence.class);
    UUID evidenceId = UUID.randomUUID();
    Instant occurred = Instant.parse("2026-08-16T10:00:00Z");
    when(users.requireUserId(authentication)).thenReturn(userId);
    when(evidence.getId()).thenReturn(evidenceId);
    when(evidence.getKind()).thenReturn(EvidenceKind.LOG_EXCERPT);
    when(evidence.getRetentionClass()).thenReturn(RetentionClass.STANDARD);
    when(evidence.getSourceSystem()).thenReturn("github");
    when(evidence.getSourceReference()).thenReturn("run-1");
    when(evidence.getOccurredAt()).thenReturn(occurred);
    when(evidence.getIngestedAt()).thenReturn(occurred.plusSeconds(1));
    when(evidence.getContentHash()).thenReturn("a".repeat(64));
    when(evidence.getContentLineCount()).thenReturn(1);
    when(search.search(any(), any(), any(), any(), any()))
        .thenReturn(
            new EvidenceSearchService.SearchPage(
                List.of(evidence), new EvidenceCursor(occurred, evidenceId)));

    EvidenceController.EvidenceSearchResponse response =
        new EvidenceController(search, users)
            .search(
                authentication, organisationId, projectId, null, null, null, null, null, null, 10);
    assertThat(response.items()).hasSize(1);
    assertThat(response.nextCursor()).isNotBlank();

    when(search.search(any(), any(), any(), any(), any()))
        .thenReturn(new EvidenceSearchService.SearchPage(List.of(evidence), null));
    EvidenceController.EvidenceSearchResponse withoutCursor =
        new EvidenceController(search, users)
            .search(
                authentication, organisationId, projectId, null, null, null, null, null, null, 10);
    assertThat(withoutCursor.nextCursor()).isNull();
  }
}
