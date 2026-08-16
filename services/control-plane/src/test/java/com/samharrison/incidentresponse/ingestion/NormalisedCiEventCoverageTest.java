package com.samharrison.incidentresponse.ingestion;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import com.samharrison.incidentresponse.organisation.Organisation;
import com.samharrison.incidentresponse.project.Project;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class NormalisedCiEventCoverageTest {
  @Test
  void rejectsNullAndEmptySourceFields() {
    assertThatThrownBy(() -> event(null)).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> event(List.of())).isInstanceOf(IllegalArgumentException.class);
  }

  private static NormalisedCiEvent event(List<String> sourceFields) {
    return new NormalisedCiEvent(
        UUID.randomUUID(),
        mock(Organisation.class),
        mock(Project.class),
        mock(EventSource.class),
        mock(WebhookDelivery.class),
        null,
        "1",
        EventProvider.GITHUB_ACTIONS,
        NormalisedEventType.PIPELINE_RUN_COMPLETED,
        Instant.now(),
        Instant.now(),
        "run",
        "build",
        1,
        PipelineRunStatus.FAILED,
        null,
        null,
        null,
        "evidence",
        sourceFields);
  }
}
