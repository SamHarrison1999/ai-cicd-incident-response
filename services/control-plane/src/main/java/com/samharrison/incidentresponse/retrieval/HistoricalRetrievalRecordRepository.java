package com.samharrison.incidentresponse.retrieval;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HistoricalRetrievalRecordRepository
    extends JpaRepository<HistoricalRetrievalRecord, UUID> {

  @Query(
      """
      select record
      from HistoricalRetrievalRecord record
      where record.organisationId = :organisationId
        and record.projectId = :projectId
        and (cast(:diagnosisCategory as String) is null or record.diagnosisCategory = :diagnosisCategory)
        and (cast(:provider as String) is null or record.provider = :provider)
        and (cast(:pipelineName as String) is null or record.pipelineName = :pipelineName)
        and (cast(:environmentName as String) is null or record.environmentName = :environmentName)
        and (cast(:gitRef as String) is null or record.gitRef = :gitRef)
        and (cast(:commitSha as String) is null or record.commitSha = :commitSha)
        and (cast(:query as String) is null or lower(record.summary) like concat('%', lower(cast(:query as String)), '%'))
        and (cast(:occurredFrom as Instant) is null or record.occurredAt >= :occurredFrom)
        and (cast(:occurredTo as Instant) is null or record.occurredAt <= :occurredTo)
        and (
          cast(:cursorOccurredAt as Instant) is null
          or record.occurredAt < :cursorOccurredAt
          or (record.occurredAt = :cursorOccurredAt and record.id < :cursorId)
        )
      order by record.occurredAt desc, record.id desc
      """)
  Slice<HistoricalRetrievalRecord> search(
      @Param("organisationId") UUID organisationId,
      @Param("projectId") UUID projectId,
      @Param("diagnosisCategory") String diagnosisCategory,
      @Param("provider") String provider,
      @Param("pipelineName") String pipelineName,
      @Param("environmentName") String environmentName,
      @Param("gitRef") String gitRef,
      @Param("commitSha") String commitSha,
      @Param("query") String query,
      @Param("occurredFrom") Instant occurredFrom,
      @Param("occurredTo") Instant occurredTo,
      @Param("cursorOccurredAt") Instant cursorOccurredAt,
      @Param("cursorId") UUID cursorId,
      Pageable pageable);
}
