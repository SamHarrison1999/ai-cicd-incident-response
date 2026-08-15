package com.samharrison.incidentresponse.feedback;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;

class FeedbackControllerContractTest {
  @Test
  void exposesReadOnlyTenantScopedFeedbackQuery() throws Exception {
    Method method =
        FeedbackController.class.getDeclaredMethod(
            "list",
            org.springframework.security.core.Authentication.class,
            java.util.UUID.class,
            java.util.UUID.class,
            String.class,
            java.time.Instant.class,
            java.time.Instant.class,
            int.class);
    assertThat(method.getAnnotation(GetMapping.class)).isNotNull();
    assertThat(
            FeedbackController.class.getAnnotation(
                    org.springframework.web.bind.annotation.RequestMapping.class)
                .value()[0])
        .contains("feedback");
  }
}
