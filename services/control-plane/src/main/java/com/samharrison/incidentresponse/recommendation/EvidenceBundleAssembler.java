package com.samharrison.incidentresponse.recommendation;

import com.samharrison.incidentresponse.evidence.Evidence;
import com.samharrison.incidentresponse.evidence.EvidenceRepository;
import com.samharrison.incidentresponse.retrieval.HistoricalRetrievalRecord;
import com.samharrison.incidentresponse.retrieval.HistoricalRetrievalRecordRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EvidenceBundleAssembler {

  private static final int MAX_ITEMS = 20;
  private final EvidenceRepository evidenceRepository;
  private final HistoricalRetrievalRecordRepository retrievalRepository;

  public EvidenceBundleAssembler(
      EvidenceRepository evidenceRepository,
      HistoricalRetrievalRecordRepository retrievalRepository) {
    this.evidenceRepository = evidenceRepository;
    this.retrievalRepository = retrievalRepository;
  }

  @Transactional(readOnly = true)
  public EvidenceBundle assemble(
      UUID organisationId, UUID projectId, List<UUID> evidenceIds, List<UUID> historicalRecordIds) {
    List<UUID> evidence = boundedIds(evidenceIds, "evidenceIds");
    List<UUID> historical = boundedIds(historicalRecordIds, "historicalRecordIds");
    if (evidence.size() + historical.size() > MAX_ITEMS) {
      throw new IllegalArgumentException("evidence bundle exceeds the permitted item count");
    }
    List<EvidenceSummary> evidenceItems = new ArrayList<>();
    for (UUID id : evidence) {
      Evidence item =
          evidenceRepository
              .findByIdAndOrganisationIdAndProjectId(id, organisationId, projectId)
              .orElseThrow(
                  () -> new IllegalArgumentException("evidence is outside the tenant scope"));
      evidenceItems.add(
          new EvidenceSummary(
              item.getId(),
              item.getKind().name(),
              item.getSourceSystem(),
              item.getContentHash(),
              item.getContent()));
    }
    List<HistoricalSummary> historicalItems = new ArrayList<>();
    for (UUID id : historical) {
      HistoricalRetrievalRecord item =
          retrievalRepository
              .findById(id)
              .filter(
                  record ->
                      organisationId.equals(record.getOrganisationId())
                          && projectId.equals(record.getProjectId()))
              .orElseThrow(
                  () ->
                      new IllegalArgumentException(
                          "historical record is outside the tenant scope"));
      historicalItems.add(
          new HistoricalSummary(
              item.getId(),
              item.getSourceKind().name(),
              item.getSummary(),
              item.getProvenanceReference()));
    }
    return new EvidenceBundle(List.copyOf(evidenceItems), List.copyOf(historicalItems));
  }

  private static List<UUID> boundedIds(List<UUID> ids, String field) {
    if (ids == null) {
      return List.of();
    }
    if (ids.stream().anyMatch(id -> id == null)) {
      throw new IllegalArgumentException(field + " must not contain null ids");
    }
    return ids.stream().distinct().toList();
  }

  public record EvidenceBundle(
      List<EvidenceSummary> evidence, List<HistoricalSummary> historical) {}

  public record EvidenceSummary(
      UUID id, String kind, String sourceSystem, String contentHash, String sanitisedContent) {}

  public record HistoricalSummary(
      UUID id, String sourceKind, String summary, String provenanceReference) {}
}
