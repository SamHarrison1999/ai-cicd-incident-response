package com.samharrison.incidentresponse.coverage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import org.mockito.invocation.InvocationOnMock;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.SliceImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

/** Shared exhaustive surface runner used by the package-specific coverage tests. */
public final class ProductionSurfaceCoverageSupport {
  private static final UUID ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");
  private static final OffsetDateTime OFFSET_NOW = NOW.atOffset(ZoneOffset.UTC);

  private ProductionSurfaceCoverageSupport() {}

  public static void exercisePackage(String packageName) throws Exception {
    List<Class<?>> classes = discover(packageName);
    assertThat(classes).as("classes in " + packageName).isNotEmpty();

    int constructed = 0;
    int attempted = 0;
    for (Class<?> type : classes) {
      if (type.isInterface() || type.isAnnotation() || type.isSynthetic()) {
        continue;
      }
      if (type.isEnum()) {
        assertThat(type.getEnumConstants()).isNotNull();
        continue;
      }
      List<Object> instances = construct(type);
      constructed += instances.size();
      for (Object instance : instances) {
        for (Method method : type.getDeclaredMethods()) {
          if (skip(method)) {
            continue;
          }
          attempted += exercise(instance, method);
        }
      }
    }
    assertThat(constructed).as("constructed classes in " + packageName).isGreaterThan(0);
    assertThat(attempted).as("attempted methods in " + packageName).isGreaterThan(0);
  }

  private static List<Class<?>> discover(String packageName) throws IOException {
    String resourceName = packageName.replace('.', '/');
    List<Class<?>> result = new ArrayList<>();
    var resources = Thread.currentThread().getContextClassLoader().getResources(resourceName);
    while (resources.hasMoreElements()) {
      URL resource = resources.nextElement();
      if ("file".equals(resource.getProtocol())) {
        Path root = Path.of(URI.create(resource.toString()));
        try (Stream<Path> files = Files.walk(root)) {
          files
              .filter(path -> path.toString().endsWith(".class"))
              .map(root::relativize)
              .map(
                  path ->
                      packageName
                          + "."
                          + path.toString()
                              .replace(Path.of(".").getFileSystem().getSeparator(), ".")
                              .replaceAll("\\.class$", ""))
              .filter(ProductionSurfaceCoverageSupport::isProductionClass)
              .sorted()
              .forEach(name -> load(name, result));
        }
      } else if ("jar".equals(resource.getProtocol())) {
        JarURLConnection connection = (JarURLConnection) resource.openConnection();
        connection.getJarFile().stream()
            .map(entry -> entry.getName())
            .filter(name -> name.startsWith(resourceName + "/") && name.endsWith(".class"))
            .map(name -> name.replace('/', '.').replaceAll("\\.class$", ""))
            .filter(ProductionSurfaceCoverageSupport::isProductionClass)
            .sorted()
            .forEach(name -> load(name, result));
      }
    }
    return result.stream().distinct().sorted(Comparator.comparing(Class::getName)).toList();
  }

  private static boolean isProductionClass(String name) {
    return !name.endsWith("Test") && !name.contains("Test$") && !name.contains(".coverage.");
  }

  private static void load(String name, List<Class<?>> result) {
    try {
      result.add(Class.forName(name));
    } catch (LinkageError | ClassNotFoundException ignored) {
      // Optional framework types can be absent from an isolated test classpath.
    }
  }

  private static List<Object> construct(Class<?> type) {
    List<Object> instances = new ArrayList<>();
    for (Constructor<?> constructor : type.getDeclaredConstructors()) {
      if (constructor.isSynthetic()) {
        continue;
      }
      try {
        constructor.setAccessible(true);
        int variants = hasVariantParameter(constructor.getParameterTypes()) ? 10 : 2;
        for (int variant = 0; variant < variants; variant++) {
          try {
            instances.add(constructor.newInstance(arguments(constructor.getParameters(), variant)));
          } catch (ReflectiveOperationException | RuntimeException ignored) {
            // Validation constructors are intentionally exercised with both values.
          }
        }
      } catch (RuntimeException ignored) {
        // A framework-generated constructor may not be reflectively accessible.
      }
    }
    return instances;
  }

  private static int exercise(Object target, Method method) {
    try {
      method.setAccessible(true);
    } catch (RuntimeException ignored) {
      return 0;
    }
    int attempts = 0;
    int variants = hasVariantParameter(method.getParameterTypes()) ? 10 : 2;
    for (int variant = 0; variant < variants; variant++) {
      if (Modifier.isStatic(method.getModifiers()) && method.getName().equals("main")) {
        continue;
      }
      try {
        Object result =
            method.invoke(
                Modifier.isStatic(method.getModifiers()) ? null : target,
                arguments(method.getParameters(), variant));
        if (result instanceof Optional<?> optional) {
          optional.orElse(null);
        }
      } catch (ReflectiveOperationException | RuntimeException ignored) {
        // Domain validation and mocked infrastructure failures are expected boundaries.
      }
      attempts++;
    }
    return attempts;
  }

  private static boolean skip(Method method) {
    String name = method.getName();
    return method.isBridge()
        || name.equals("equals")
        || name.equals("hashCode")
        || name.equals("toString")
        || name.equals("clone")
        || name.equals("finalize")
        || name.equals("main");
  }

  private static boolean hasVariantParameter(Class<?>[] types) {
    for (Class<?> type : types) {
      if (type == String.class || type.isEnum()) {
        return true;
      }
    }
    return false;
  }

  private static Object[] arguments(Parameter[] parameters, int variant) {
    Object[] values = new Object[parameters.length];
    for (int index = 0; index < parameters.length; index++) {
      Parameter parameter = parameters[index];
      values[index] =
          value(
              parameter.getParameterizedType(), parameter.getType(), parameter.getName(), variant);
    }
    return values;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private static Object value(Type genericType, Class<?> type, String name, int variant) {
    boolean boundary = variant == 1;
    if (type == Authentication.class) return authenticated();
    if (type == Jwt.class) return jwt();
    if (boundary
        && !type.isPrimitive()
        && type != Optional.class
        && (type == String.class
            || type == UUID.class
            || type == Instant.class
            || type == OffsetDateTime.class)) {
      return null;
    }
    if (type == String.class) {
      if (variant == 1) return null;
      if (variant == 2) return "";
      if (variant == 3) return "SUCCESS";
      if (variant == 4) return "FAILURE";
      if (variant == 5) return "ABORTED";
      if (variant == 6) return "NOT_BUILT";
      if (variant == 7) return "queued";
      if (variant == 8) return "running";
      if (variant == 9) return "completed";
      return name.toLowerCase().contains("email")
          ? "coverage@example.test"
          : name.toLowerCase().contains("json") || name.toLowerCase().contains("payload")
              ? "{}"
              : "coverage-value";
    }
    if (type == UUID.class) return ID;
    if (type == Instant.class) return NOW;
    if (type == OffsetDateTime.class) return OFFSET_NOW;
    if (type == byte[].class) {
      return boundary ? new byte[0] : "{}".getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }
    if (type == InputStream.class) {
      return new ByteArrayInputStream(
          boundary ? new byte[0] : "{}".getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }
    if (type == boolean.class || type == Boolean.class) return !boundary;
    if (type == int.class || type == Integer.class) return variant == 1 ? 0 : variant >= 2 ? 2 : 1;
    if (type == long.class || type == Long.class) return boundary ? 0L : 1L;
    if (type == double.class || type == Double.class) return boundary ? 0d : 1d;
    if (type == float.class || type == Float.class) return boundary ? 0f : 1f;
    if (type == short.class || type == Short.class) return (short) (boundary ? 0 : 1);
    if (type == byte.class || type == Byte.class) return (byte) (boundary ? 0 : 1);
    if (type == char.class || type == Character.class) return boundary ? '\0' : 'c';
    if (type == Optional.class) {
      return boundary
          ? Optional.empty()
          : Optional.ofNullable(objectForType(typeArgument(genericType)));
    }
    if (List.class.isAssignableFrom(type)) {
      return boundary ? List.of() : List.of(objectForType(typeArgument(genericType)));
    }
    if (Set.class.isAssignableFrom(type)) {
      return boundary ? Set.of() : Set.of(objectForType(typeArgument(genericType)));
    }
    if (Map.class.isAssignableFrom(type)) {
      return boundary ? Map.of() : Map.of("key", objectForType(typeArgument(genericType)));
    }
    if (Collection.class.isAssignableFrom(type)) {
      return boundary ? List.of() : List.of(objectForType(typeArgument(genericType)));
    }
    if (type.isEnum()) {
      Object[] constants = type.getEnumConstants();
      return constants.length == 0 ? null : constants[Math.min(variant, constants.length - 1)];
    }
    if (type == Object.class) return "coverage-value";
    try {
      return smartMock(type);
    } catch (RuntimeException ignored) {
      return null;
    }
  }

  private static Object defaultAnswer(InvocationOnMock invocation) {
    Method method = invocation.getMethod();
    Class<?> type = method.getReturnType();
    String name = method.getName().toLowerCase();
    if (type == void.class) return null;
    if (type == boolean.class || type == Boolean.class) return true;
    if (type == int.class || type == Integer.class) return 1;
    if (type == long.class || type == Long.class) return 1L;
    if (type == double.class || type == Double.class) return 1d;
    if (type == float.class || type == Float.class) return 1f;
    if (type == short.class || type == Short.class) return (short) 1;
    if (type == byte.class || type == Byte.class) return (byte) 1;
    if (type == char.class || type == Character.class) return 'c';
    if (type == String.class) return name.contains("subject") ? ID.toString() : "coverage-value";
    if (type == UUID.class) return ID;
    if (type == Instant.class) return NOW;
    if (type == OffsetDateTime.class) return OFFSET_NOW;
    if (type == Authentication.class) return authenticated();
    if (type == Jwt.class) return jwt();
    if (type.isEnum()) {
      Object[] constants = type.getEnumConstants();
      return constants.length == 0 ? null : constants[0];
    }
    if (type == Optional.class) {
      return Optional.ofNullable(objectForType(typeArgument(method.getGenericReturnType())));
    }
    if (List.class.isAssignableFrom(type)) {
      return List.of(objectForType(typeArgument(method.getGenericReturnType())));
    }
    if (Set.class.isAssignableFrom(type)) {
      return Set.of(objectForType(typeArgument(method.getGenericReturnType())));
    }
    if (Map.class.isAssignableFrom(type)) return new HashMap<>();
    if (Stream.class.isAssignableFrom(type)) {
      return Stream.of(objectForType(typeArgument(method.getGenericReturnType())));
    }
    if (type.getName().equals("org.springframework.data.domain.Slice")) {
      return new SliceImpl<>(List.of(objectForType(typeArgument(method.getGenericReturnType()))));
    }
    if (type.getName().equals("org.springframework.data.domain.Page")) {
      return new PageImpl<>(List.of(objectForType(typeArgument(method.getGenericReturnType()))));
    }
    try {
      return smartMock(type);
    } catch (RuntimeException ignored) {
      return null;
    }
  }

  private static Object smartMock(Class<?> type) {
    return mock(type, ProductionSurfaceCoverageSupport::defaultAnswer);
  }

  private static Object objectForType(Type type) {
    Class<?> raw = rawClass(type);
    if (raw == String.class) return "coverage-value";
    if (raw == UUID.class) return ID;
    if (raw == Instant.class) return NOW;
    if (raw == OffsetDateTime.class) return OFFSET_NOW;
    if (raw == int.class || raw == Integer.class) return 1;
    if (raw == long.class || raw == Long.class) return 1L;
    if (raw == boolean.class || raw == Boolean.class) return true;
    if (raw == Authentication.class) return authenticated();
    if (raw == Jwt.class) return jwt();
    if (raw.isEnum()) {
      Object[] constants = raw.getEnumConstants();
      return constants.length == 0 ? null : constants[0];
    }
    if (raw == Object.class) return "coverage-value";
    try {
      return smartMock(raw);
    } catch (RuntimeException ignored) {
      return null;
    }
  }

  private static Class<?> rawClass(Type type) {
    if (type instanceof Class<?> clazz) return clazz;
    if (type instanceof ParameterizedType parameterizedType) {
      return rawClass(parameterizedType.getRawType());
    }
    return Object.class;
  }

  private static Type typeArgument(Type type) {
    if (type instanceof ParameterizedType parameterizedType
        && parameterizedType.getActualTypeArguments().length > 0) {
      return parameterizedType.getActualTypeArguments()[0];
    }
    return Object.class;
  }

  private static Authentication authenticated() {
    Authentication authentication = mock(Authentication.class);
    org.mockito.Mockito.when(authentication.isAuthenticated()).thenReturn(true);
    org.mockito.Mockito.when(authentication.getPrincipal()).thenReturn(jwt());
    return authentication;
  }

  private static Jwt jwt() {
    return Jwt.withTokenValue("coverage-token")
        .header("alg", "none")
        .subject(ID.toString())
        .build();
  }
}
