package com.samharrison.incidentresponse.retrieval;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HistoricalRetrievalQueryService {

  private final HistoricalRetrievalRecordRepository repository;

  public HistoricalRetrievalQueryService(HistoricalRetrievalRecordRepository repository) {
    this.repository = repository;
  }

  @Transactional(readOnly = true)
  public Slice<HistoricalRetrievalRecord> search(
      UUID organisationId,
      UUID projectId,
      HistoricalRetrievalCriteria criteria,
      Instant cursorOccurredAt,
      UUID cursorId) {
    return repository.search(
        organisationId,
        projectId,
        criteria.diagnosisCategory(),
        criteria.provider(),
        criteria.pipelineName(),
        criteria.environmentName(),
        criteria.gitRef(),
        criteria.commitSha(),
        criteria.query(),
        criteria.occurredFrom(),
        criteria.occurredTo(),
        cursorOccurredAt,
        cursorId,
        PageRequest.of(0, criteria.limit()));
  }
}
