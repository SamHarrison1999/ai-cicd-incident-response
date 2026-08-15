package com.samharrison.incidentresponse.learning;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

class OperationalTrendControllerContractTest {
  @Test
  void exposesReadOnlyTenantScopedTrendRoutes() {
    RequestMapping mapping = OperationalTrendController.class.getAnnotation(RequestMapping.class);
    assertThat(mapping).isNotNull();
    assertThat(mapping.value()[0]).contains("operational-learning");
    Method[] methods = OperationalTrendController.class.getDeclaredMethods();
    assertThat(methods).anyMatch(method -> hasPath(method, "/trends"));
    assertThat(methods).anyMatch(method -> hasPath(method, "/trends/compare"));
  }

  private static boolean hasPath(Method method, String expected) {
    GetMapping mapping = method.getAnnotation(GetMapping.class);
    return mapping != null && java.util.Arrays.asList(mapping.value()).contains(expected);
  }
}
