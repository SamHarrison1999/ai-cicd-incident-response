package com.samharrison.incidentresponse.incident;

import com.samharrison.incidentresponse.evidence.Evidence;
import com.samharrison.incidentresponse.ingestion.NormalisedCiEvent;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
    name = "evidence_event_links",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_evidence_event_link_pair",
            columnNames = {"evidence_id", "event_id"}))
public class EvidenceEventLink {

  @Id private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "evidence_id", nullable = false)
  private Evidence evidence;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "event_id", nullable = false)
  private NormalisedCiEvent event;

  @Column(name = "organisation_id", nullable = false, updatable = false)
  private UUID organisationId;

  @Column(name = "project_id", nullable = false, updatable = false)
  private UUID projectId;

  @Column(name = "linked_at", nullable = false)
  private Instant linkedAt;

  protected EvidenceEventLink() {}

  public EvidenceEventLink(UUID id, Evidence evidence, NormalisedCiEvent event, Instant linkedAt) {
    this.id = Objects.requireNonNull(id);
    this.evidence = Objects.requireNonNull(evidence);
    this.event = Objects.requireNonNull(event);
    this.organisationId = evidence.getOrganisation().getId();
    this.projectId = evidence.getProject().getId();
    this.linkedAt = Objects.requireNonNull(linkedAt);
  }

  public UUID getId() {
    return id;
  }

  public Evidence getEvidence() {
    return evidence;
  }

  public NormalisedCiEvent getEvent() {
    return event;
  }

  public Instant getLinkedAt() {
    return linkedAt;
  }
}
