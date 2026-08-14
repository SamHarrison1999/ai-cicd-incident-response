package com.samharrison.incidentresponse.incident;

import com.samharrison.incidentresponse.ingestion.NormalisedCiEvent;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
    name = "incident_event_links",
    uniqueConstraints =
        @UniqueConstraint(name = "uk_incident_event_link_event", columnNames = "event_id"))
public class IncidentEventLink {

  @Id private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "incident_id", nullable = false)
  private Incident incident;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "event_id", nullable = false, unique = true)
  private NormalisedCiEvent event;

  @Column(name = "organisation_id", nullable = false, updatable = false)
  private UUID organisationId;

  @Column(name = "project_id", nullable = false, updatable = false)
  private UUID projectId;

  @Column(name = "linked_at", nullable = false)
  private Instant linkedAt;

  protected IncidentEventLink() {}

  public IncidentEventLink(UUID id, Incident incident, NormalisedCiEvent event, Instant linkedAt) {
    this.id = Objects.requireNonNull(id);
    this.incident = Objects.requireNonNull(incident);
    this.event = Objects.requireNonNull(event);
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

  public NormalisedCiEvent getEvent() {
    return event;
  }

  public Instant getLinkedAt() {
    return linkedAt;
  }
}
