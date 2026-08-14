package com.samharrison.incidentresponse.incident;

import com.samharrison.incidentresponse.evidence.Evidence;
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
    name = "incident_evidence_links",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_incident_evidence_link_pair",
            columnNames = {"incident_id", "evidence_id"}))
public class IncidentEvidenceLink {

  @Id private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "incident_id", nullable = false)
  private Incident incident;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "evidence_id", nullable = false)
  private Evidence evidence;

  @Column(name = "organisation_id", nullable = false, updatable = false)
  private UUID organisationId;

  @Column(name = "project_id", nullable = false, updatable = false)
  private UUID projectId;

  @Column(name = "linked_at", nullable = false)
  private Instant linkedAt;

  protected IncidentEvidenceLink() {}

  public IncidentEvidenceLink(UUID id, Incident incident, Evidence evidence, Instant linkedAt) {
    this.id = Objects.requireNonNull(id);
    this.incident = Objects.requireNonNull(incident);
    this.evidence = Objects.requireNonNull(evidence);
    this.organisationId = incident.getOrganisation().getId();
    this.projectId = incident.getProject().getId();
    this.linkedAt = Objects.requireNonNull(linkedAt);
  }

  public UUID getId() {
    return id;
  }

  public Incident getIncident() {
    return incident;
  }

  public Evidence getEvidence() {
    return evidence;
  }

  public Instant getLinkedAt() {
    return linkedAt;
  }
}
