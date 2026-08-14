package com.samharrison.incidentresponse.ingestion;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NormalisedCiEventRepository extends JpaRepository<NormalisedCiEvent, UUID> {

  java.util.Optional<NormalisedCiEvent> findByWebhookDeliveryId(UUID webhookDeliveryId);

  java.util.Optional<NormalisedCiEvent> findByIdAndOrganisationId(UUID id, UUID organisationId);

  java.util.List<NormalisedCiEvent> findAllByProjectIdAndOrganisationIdOrderByOccurredAtAsc(
      UUID projectId, UUID organisationId);

  java.util.List<NormalisedCiEvent>
      findAllByProjectIdAndOrganisationIdOrderByOccurredAtAscReceivedAtAscIdAsc(
          UUID projectId, UUID organisationId);

  @Query(
      """
      select event
      from NormalisedCiEvent event
      where event.project.id = :projectId
        and event.organisation.id = :organisationId
        and (:status is null or event.pipelineStatus = :status)
        and (:branch is null or event.gitRef = :branch)
        and (:commitSha is null or event.commitSha = :commitSha)
        and (:environment is null or event.environmentName = :environment)
        and (:eventType is null or event.eventType = :eventType)
        and (:fromTime is null or event.occurredAt >= :fromTime)
        and (:toTime is null or event.occurredAt <= :toTime)
        and (
          :cursorOccurredAt is null
          or event.occurredAt < :cursorOccurredAt
          or (event.occurredAt = :cursorOccurredAt and event.receivedAt < :cursorReceivedAt)
          or (event.occurredAt = :cursorOccurredAt
              and event.receivedAt = :cursorReceivedAt
              and event.id < :cursorId)
        )
      order by event.occurredAt desc, event.receivedAt desc, event.id desc
      """)
  Slice<NormalisedCiEvent> searchTimeline(
      @Param("projectId") UUID projectId,
      @Param("organisationId") UUID organisationId,
      @Param("status") PipelineRunStatus status,
      @Param("branch") String branch,
      @Param("commitSha") String commitSha,
      @Param("environment") String environment,
      @Param("eventType") NormalisedEventType eventType,
      @Param("fromTime") Instant fromTime,
      @Param("toTime") Instant toTime,
      @Param("cursorOccurredAt") Instant cursorOccurredAt,
      @Param("cursorReceivedAt") Instant cursorReceivedAt,
      @Param("cursorId") UUID cursorId,
      Pageable pageable);
}
