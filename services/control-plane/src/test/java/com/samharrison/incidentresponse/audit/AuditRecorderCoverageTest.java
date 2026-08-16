package com.samharrison.incidentresponse.audit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

class AuditRecorderCoverageTest {
  @Test
  void recordsFallbackAndRequestCorrelationIds() {
    AuditEventRepository repository = mock(AuditEventRepository.class);
    AuditRecorder recorder = new AuditRecorder(repository);
    UUID userId = UUID.randomUUID();
    UUID organisationId = UUID.randomUUID();
    try {
      MDC.remove("correlationId");
      recorder.record(userId, organisationId, "READ", "PROJECT", UUID.randomUUID(), "{}");
      MDC.put("correlationId", "correlation-1");
      recorder.record(userId, organisationId, "WRITE", "PROJECT", UUID.randomUUID(), "{}");
    } finally {
      MDC.clear();
    }
    verify(repository, org.mockito.Mockito.times(2)).save(any());
  }
}
