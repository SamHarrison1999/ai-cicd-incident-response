package com.samharrison.incidentresponse.audit;

import java.time.Instant;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

@Service
public class AuditRecorder {

  private final AuditEventRepository auditEventRepository;

  public AuditRecorder(AuditEventRepository auditEventRepository) {
    this.auditEventRepository = auditEventRepository;
  }

  public void record(
      UUID actorUserId,
      UUID organisationId,
      String action,
      String targetType,
      UUID targetId,
      String metadata) {
    String correlationId = MDC.get("correlationId");
    auditEventRepository.save(
        new AuditEvent(
            UUID.randomUUID(),
            actorUserId,
            organisationId,
            action,
            targetType,
            targetId,
            correlationId == null ? "unavailable" : correlationId,
            metadata,
            Instant.now()));
  }
}
