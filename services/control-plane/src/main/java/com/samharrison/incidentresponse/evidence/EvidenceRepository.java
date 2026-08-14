package com.samharrison.incidentresponse.evidence;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EvidenceRepository extends JpaRepository<Evidence, UUID> {
  List<Evidence> findAllByOrganisationIdAndProjectIdOrderByOccurredAtDescIdDesc(
      UUID organisationId, UUID projectId);

  Optional<Evidence> findByIdAndOrganisationIdAndProjectId(
      UUID id, UUID organisationId, UUID projectId);

  @Query(
      """
      select evidence
      from Evidence evidence
      where evidence.organisation.id = :organisationId
        and evidence.project.id = :projectId
        and (:kind is null or evidence.kind = :kind)
        and (:sourceSystem is null or evidence.sourceSystem = :sourceSystem)
        and (:query is null or lower(evidence.content) like lower(concat('%', :query, '%')))
        and (:occurredFrom is null or evidence.occurredAt >= :occurredFrom)
        and (:occurredTo is null or evidence.occurredAt <= :occurredTo)
        and (
          :cursorOccurredAt is null
          or evidence.occurredAt < :cursorOccurredAt
          or (evidence.occurredAt = :cursorOccurredAt and evidence.id < :cursorId)
        )
      order by evidence.occurredAt desc, evidence.id desc
      """)
  Slice<Evidence> search(
      @Param("organisationId") UUID organisationId,
      @Param("projectId") UUID projectId,
      @Param("kind") EvidenceKind kind,
      @Param("sourceSystem") String sourceSystem,
      @Param("query") String query,
      @Param("occurredFrom") Instant occurredFrom,
      @Param("occurredTo") Instant occurredTo,
      @Param("cursorOccurredAt") Instant cursorOccurredAt,
      @Param("cursorId") UUID cursorId,
      Pageable pageable);
}
