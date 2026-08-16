package com.samharrison.incidentresponse.coverage;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import com.samharrison.incidentresponse.ControlPlaneApplication;
import com.samharrison.incidentresponse.authentication.TokenHasher;
import com.samharrison.incidentresponse.evidence.EvidenceContentHasher;
import com.samharrison.incidentresponse.ingestion.WebhookSignatureService;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

class ImpossibleBranchCoverageTest {

  @Test
  void coversUnavailableSha256DefensiveBranches() {
    try (MockedStatic<MessageDigest> messageDigest = mockStatic(MessageDigest.class)) {
      messageDigest
          .when(() -> MessageDigest.getInstance("SHA-256"))
          .thenThrow(new NoSuchAlgorithmException("test"));

      assertThatThrownBy(() -> EvidenceContentHasher.sha256Hex("value"))
          .isInstanceOf(IllegalStateException.class);
      assertThatThrownBy(() -> new TokenHasher().hash("value"))
          .isInstanceOf(IllegalStateException.class);
      assertThatThrownBy(() -> new WebhookSignatureService().sha256Hex(new byte[] {1}))
          .isInstanceOf(IllegalStateException.class);
    }
  }

  @Test
  void delegatesApplicationStartupToSpringBoot() {
    try (MockedStatic<SpringApplication> spring = mockStatic(SpringApplication.class)) {
      ConfigurableApplicationContext context = mock(ConfigurableApplicationContext.class);
      spring
          .when(() -> SpringApplication.run(ControlPlaneApplication.class, new String[] {"--test"}))
          .thenReturn(context);

      ControlPlaneApplication.main(new String[] {"--test"});

      spring.verify(
          () -> SpringApplication.run(ControlPlaneApplication.class, new String[] {"--test"}));
    }
  }
}
