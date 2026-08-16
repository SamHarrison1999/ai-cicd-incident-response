package com.samharrison.incidentresponse.recommendation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.samharrison.incidentresponse.evidence.Evidence;
import com.samharrison.incidentresponse.evidence.EvidenceKind;
import com.samharrison.incidentresponse.evidence.EvidenceRepository;
import com.samharrison.incidentresponse.evidence.RetentionClass;
import com.samharrison.incidentresponse.organisation.Organisation;
import com.samharrison.incidentresponse.project.Project;
import com.samharrison.incidentresponse.project.ProjectStatus;
import com.samharrison.incidentresponse.retrieval.HistoricalRetrievalRecord;
import com.samharrison.incidentresponse.retrieval.HistoricalRetrievalRecordRepository;
import com.samharrison.incidentresponse.retrieval.HistoricalSourceKind;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EvidenceBundleAssemblerCoverageTest {

  private static final Instant NOW = Instant.parse("2026-08-16T12:00:00Z");

  @Mock private EvidenceRepository evidenceRepository;
  @Mock private HistoricalRetrievalRecordRepository retrievalRepository;

  private EvidenceBundleAssembler assembler;
  private UUID organisationId;
  private UUID projectId;
  private Evidence evidence;
  private HistoricalRetrievalRecord historical;

  @BeforeEach
  void setUp() {
    assembler = new EvidenceBundleAssembler(evidenceRepository, retrievalRepository);
    organisationId = UUID.randomUUID();
    projectId = UUID.randomUUID();
    Organisation organisation = new Organisation(organisationId, "Example", "example");
    Project project =
        new Project(projectId, organisation, "API", "api", "Description", ProjectStatus.ACTIVE);
    evidence =
        new Evidence(
            UUID.randomUUID(),
            organisation,
            project,
            EvidenceKind.LOG_EXCERPT,
            RetentionClass.STANDARD,
            "github",
            "run-1",
            NOW,
            NOW,
            "a".repeat(64),
            "sanitised content",
            1);
    historical =
        new HistoricalRetrievalRecord(
            UUID.randomUUID(),
            organisationId,
            projectId,
            UUID.randomUUID(),
            HistoricalSourceKind.INCIDENT,
            UUID.randomUUID(),
            NOW,
            "github",
            "api",
            "production",
            "main",
            "a".repeat(40),
            "DEPENDENCY_FAILURE",
            "Historical summary",
            "Matching project and diagnosis",
            "incident:123",
            NOW);
  }

  @Test
  void assemblesTenantScopedEvidenceAndHistoricalItems() {
    when(evidenceRepository.findByIdAndOrganisationIdAndProjectId(
            evidence.getId(), organisationId, projectId))
        .thenReturn(Optional.of(evidence));
    when(retrievalRepository.findById(historical.getId())).thenReturn(Optional.of(historical));

    EvidenceBundleAssembler.EvidenceBundle result =
        assembler.assemble(
            organisationId,
            projectId,
            List.of(evidence.getId(), evidence.getId()),
            List.of(historical.getId()));

    assertThat(result.evidence()).hasSize(1);
    assertThat(result.evidence().getFirst().sanitisedContent()).isEqualTo("sanitised content");
    assertThat(result.historical()).hasSize(1);
    assertThat(result.historical().getFirst().provenanceReference()).isEqualTo("incident:123");
  }

  @Test
  void nullListsProduceAnEmptyBundle() {
    EvidenceBundleAssembler.EvidenceBundle result =
        assembler.assemble(organisationId, projectId, null, null);

    assertThat(result.evidence()).isEmpty();
    assertThat(result.historical()).isEmpty();
  }

  @Test
  void nullIdsAreRejected() {
    assertThatThrownBy(
            () ->
                assembler.assemble(
                    organisationId,
                    projectId,
                    java.util.Collections.<UUID>singletonList(null),
                    null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("evidenceIds");
  }

  @Test
  void bundleSizeIsBounded() {
    List<UUID> ids = java.util.stream.Stream.generate(UUID::randomUUID).limit(21).toList();

    assertThatThrownBy(() -> assembler.assemble(organisationId, projectId, ids, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("exceeds");
  }

  @Test
  void missingEvidenceIsRejected() {
    UUID id = UUID.randomUUID();
    when(evidenceRepository.findByIdAndOrganisationIdAndProjectId(id, organisationId, projectId))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> assembler.assemble(organisationId, projectId, List.of(id), null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("evidence is outside the tenant scope");
  }

  @Test
  void historicalRecordOutsideTenantIsRejected() {
    HistoricalRetrievalRecord outside =
        new HistoricalRetrievalRecord(
            historical.getId(),
            UUID.randomUUID(),
            projectId,
            historical.getIncidentId(),
            HistoricalSourceKind.INCIDENT,
            historical.getSourceId(),
            NOW,
            null,
            null,
            null,
            null,
            null,
            null,
            "Summary",
            "Explanation",
            "provenance",
            NOW);
    when(retrievalRepository.findById(historical.getId())).thenReturn(Optional.of(outside));

    assertThatThrownBy(
            () -> assembler.assemble(organisationId, projectId, null, List.of(historical.getId())))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("historical record is outside the tenant scope");
  }
}
