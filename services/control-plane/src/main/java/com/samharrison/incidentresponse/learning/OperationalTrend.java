package com.samharrison.incidentresponse.learning;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "operational_trends")
public class OperationalTrend {
  @Id private UUID id;

  @Column(name = "organisation_id", nullable = false)
  private UUID organisationId;

  @Column(name = "project_id", nullable = false)
  private UUID projectId;

  @Enumerated(EnumType.STRING)
  @Column(name = "dimension", nullable = false, length = 32)
  private TrendDimension dimension;

  @Column(name = "dimension_key", nullable = false, length = 96)
  private String dimensionKey;

  @Column(name = "window_start", nullable = false)
  private Instant windowStart;

  @Column(name = "window_end", nullable = false)
  private Instant windowEnd;

  @Column(name = "aggregation_version", nullable = false, length = 64)
  private String aggregationVersion;

  @Column(name = "sample_count", nullable = false)
  private int sampleCount;

  @Column(name = "observed_count", nullable = false)
  private int observedCount;

  @Column(name = "source_reference", nullable = false, length = 128)
  private String sourceReference;

  @Enumerated(EnumType.STRING)
  @Column(name = "suppression_reason", nullable = false, length = 32)
  private TrendSuppressionReason suppressionReason;

  protected OperationalTrend() {}

  public OperationalTrend(
      UUID id,
      UUID organisationId,
      UUID projectId,
      TrendDimension dimension,
      String dimensionKey,
      ObservationWindow window,
      String aggregationVersion,
      int sampleCount,
      int observedCount,
      String sourceReference,
      TrendSuppressionReason suppressionReason) {
    this.id = Objects.requireNonNull(id);
    this.organisationId = Objects.requireNonNull(organisationId);
    this.projectId = Objects.requireNonNull(projectId);
    this.dimension = Objects.requireNonNull(dimension);
    this.dimensionKey = Objects.requireNonNull(dimensionKey);
    this.windowStart = window.start();
    this.windowEnd = window.end();
    this.aggregationVersion = Objects.requireNonNull(aggregationVersion);
    this.sourceReference = Objects.requireNonNull(sourceReference);
    this.suppressionReason = Objects.requireNonNull(suppressionReason);
    if (sampleCount < 0 || observedCount < 0 || observedCount > sampleCount) {
      throw new IllegalArgumentException("trend counts are invalid");
    }
    this.sampleCount = sampleCount;
    this.observedCount = observedCount;
  }

  public UUID getId() {
    return id;
  }

  public UUID getOrganisationId() {
    return organisationId;
  }

  public UUID getProjectId() {
    return projectId;
  }

  public TrendDimension getDimension() {
    return dimension;
  }

  public String getDimensionKey() {
    return dimensionKey;
  }

  public Instant getWindowStart() {
    return windowStart;
  }

  public Instant getWindowEnd() {
    return windowEnd;
  }

  public String getAggregationVersion() {
    return aggregationVersion;
  }

  public int getSampleCount() {
    return sampleCount;
  }

  public int getObservedCount() {
    return observedCount;
  }

  public String getSourceReference() {
    return sourceReference;
  }

  public TrendSuppressionReason getSuppressionReason() {
    return suppressionReason;
  }
}
