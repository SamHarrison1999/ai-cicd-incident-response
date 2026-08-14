package com.samharrison.incidentresponse.evidence;

import com.samharrison.incidentresponse.organisation.Organisation;
import com.samharrison.incidentresponse.project.Project;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
    name = "evidence_items",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_evidence_tenant_hash",
            columnNames = {"organisation_id", "project_id", "content_hash"}))
public class Evidence {

  @Id private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "organisation_id", nullable = false)
  private Organisation organisation;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "project_id", nullable = false)
  private Project project;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private EvidenceKind kind;

  @Enumerated(EnumType.STRING)
  @Column(name = "retention_class", nullable = false, length = 32)
  private RetentionClass retentionClass;

  @Column(name = "source_system", nullable = false, length = 80)
  private String sourceSystem;

  @Column(name = "source_reference", nullable = false, length = 200)
  private String sourceReference;

  @Column(name = "occurred_at", nullable = false)
  private Instant occurredAt;

  @Column(name = "ingested_at", nullable = false)
  private Instant ingestedAt;

  @Column(name = "content_hash", nullable = false, length = 64)
  private String contentHash;

  @Column(nullable = false, columnDefinition = "text")
  private String content;

  @Column(name = "content_line_count", nullable = false)
  private int contentLineCount;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  protected Evidence() {}

  public Evidence(
      UUID id,
      Organisation organisation,
      Project project,
      EvidenceKind kind,
      RetentionClass retentionClass,
      String sourceSystem,
      String sourceReference,
      Instant occurredAt,
      Instant ingestedAt,
      String contentHash,
      String content,
      int contentLineCount) {
    this.id = Objects.requireNonNull(id);
    this.organisation = Objects.requireNonNull(organisation);
    this.project = Objects.requireNonNull(project);
    this.kind = Objects.requireNonNull(kind);
    this.retentionClass = Objects.requireNonNull(retentionClass);
    this.sourceSystem = requireText(sourceSystem, "sourceSystem");
    this.sourceReference = requireText(sourceReference, "sourceReference");
    this.occurredAt = Objects.requireNonNull(occurredAt);
    this.ingestedAt = Objects.requireNonNull(ingestedAt);
    this.contentHash = requireHash(contentHash);
    this.content = requireText(content, "content");
    if (contentLineCount <= 0 || contentLineCount > EvidenceRedactor.MAX_LINES + 1) {
      throw new IllegalArgumentException("contentLineCount is outside the permitted range");
    }
    if (content.length() > EvidenceRedactor.MAX_CHARS) {
      throw new IllegalArgumentException("content exceeds the permitted size");
    }
    this.contentLineCount = contentLineCount;
    this.createdAt = ingestedAt;
  }

  public UUID getId() {
    return id;
  }

  public Organisation getOrganisation() {
    return organisation;
  }

  public Project getProject() {
    return project;
  }

  public EvidenceKind getKind() {
    return kind;
  }

  public RetentionClass getRetentionClass() {
    return retentionClass;
  }

  public String getSourceSystem() {
    return sourceSystem;
  }

  public String getSourceReference() {
    return sourceReference;
  }

  public Instant getOccurredAt() {
    return occurredAt;
  }

  public Instant getIngestedAt() {
    return ingestedAt;
  }

  public String getContentHash() {
    return contentHash;
  }

  public String getContent() {
    return content;
  }

  public int getContentLineCount() {
    return contentLineCount;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  private static String requireText(String value, String fieldName) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(fieldName + " must not be blank");
    }
    return value;
  }

  private static String requireHash(String value) {
    if (value == null || !value.matches("[0-9a-f]{64}")) {
      throw new IllegalArgumentException("contentHash must be lowercase SHA-256 hex");
    }
    return value;
  }
}
