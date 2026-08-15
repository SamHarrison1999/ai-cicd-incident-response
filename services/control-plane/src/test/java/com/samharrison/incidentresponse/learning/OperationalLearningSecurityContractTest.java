package com.samharrison.incidentresponse.learning;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

class OperationalLearningSecurityContractTest {
  @Test
  void exposesOnlyTenantScopedReadRoutes() {
    RequestMapping mapping = OperationalTrendController.class.getAnnotation(RequestMapping.class);
    if (mapping != null) {
      assertThat(mapping.value()[0]).contains("organisations").contains("operational-learning");
    }

    boolean hasTenantScopedReadEndpoint = false;
    for (Method method : OperationalTrendController.class.getDeclaredMethods()) {
      if (method.getAnnotation(GetMapping.class) == null
          && method.getAnnotation(PostMapping.class) == null
          && method.getAnnotation(RequestMapping.class) == null) {
        continue;
      }

      assertThat(method.getAnnotation(PostMapping.class)).isNull();
      GetMapping getMapping = method.getAnnotation(GetMapping.class);
      if (getMapping == null) {
        continue;
      }

      for (String path : getMapping.value()) {
        if (path.contains("organisations") && path.contains("operational-learning")) {
          hasTenantScopedReadEndpoint = true;
        }
      }
    }

    if (mapping == null) {
      assertThat(hasTenantScopedReadEndpoint).isTrue();
    }
  }
}
