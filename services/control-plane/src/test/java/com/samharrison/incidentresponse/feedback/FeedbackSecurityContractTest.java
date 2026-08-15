package com.samharrison.incidentresponse.feedback;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

class FeedbackSecurityContractTest {
  @Test
  void feedbackControllerIsReadOnlyAndUsesGet() {
    Method[] methods = FeedbackController.class.getDeclaredMethods();
    assertThat(methods).anyMatch(method -> method.getAnnotation(GetMapping.class) != null);
    assertThat(methods)
        .noneMatch(
            method ->
                method.getAnnotation(PostMapping.class) != null
                    || method.getAnnotation(PutMapping.class) != null
                    || method.getAnnotation(DeleteMapping.class) != null);
  }
}
