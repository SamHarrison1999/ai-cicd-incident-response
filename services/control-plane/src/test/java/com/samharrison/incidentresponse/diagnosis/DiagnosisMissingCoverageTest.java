package com.samharrison.incidentresponse.diagnosis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.samharrison.incidentresponse.audit.AuditRecorder;
import com.samharrison.incidentresponse.evidence.EvidenceRepository;
import com.samharrison.incidentresponse.project.ProjectRepository;
import com.samharrison.incidentresponse.tenancy.TenantAccessService;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

class DiagnosisMissingCoverageTest {

  @Test
  void diagnosisUsesWritableTransactionBecauseViewingIsAudited() throws Exception {
    Transactional transactional =
        DiagnosisService.class
            .getMethod("diagnose", UUID.class, UUID.class, UUID.class)
            .getAnnotation(Transactional.class);

    assertThat(transactional).isNotNull();
    assertThat(transactional.readOnly()).isFalse();
  }

  @Test
  void rejectsDiagnosisForAProjectOutsideTheTenant() {
    ProjectRepository projects = mock(ProjectRepository.class);
    UUID organisationId = UUID.randomUUID();
    UUID projectId = UUID.randomUUID();
    when(projects.findByIdAndOrganisationId(projectId, organisationId))
        .thenReturn(Optional.empty());
    DiagnosisService service =
        new DiagnosisService(
            mock(EvidenceRepository.class),
            projects,
            mock(TenantAccessService.class),
            mock(AuditRecorder.class),
            mock(DeterministicDiagnosisEngine.class));

    assertThatThrownBy(() -> service.diagnose(UUID.randomUUID(), organisationId, projectId))
        .isInstanceOf(com.samharrison.incidentresponse.tenancy.TenantAccessException.class);
  }
}
