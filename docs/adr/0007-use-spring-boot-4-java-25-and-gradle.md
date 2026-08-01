# ADR 0007: Use Spring Boot 4, Java 25, and Gradle

- Status: Accepted
- Date: 2026-08-01
- Decision owners: Project maintainer
- Related phase: Phase 1

## Context

The Java control plane requires a current LTS Java runtime, production-oriented Spring support, reproducible builds, dependency management, testing, code quality checks, and container packaging.

Java 25 is the current LTS line. Spring Framework 7 and Spring Boot 4 are designed for the current Java ecosystem while retaining mature Spring programming models. Gradle supports running on Java 25 from Gradle 9.1 onward.

## Decision

Use:

- Java 25 as the compilation and runtime toolchain.
- Spring Boot 4.0.x during the initial foundation.
- Gradle with Kotlin DSL and the checked-in wrapper.
- Spring Boot dependency management.
- JUnit and Testcontainers for tests.
- Spotless, Checkstyle, and JaCoCo for quality controls.

Spring Boot patch versions may be updated through normal dependency maintenance. Minor-version upgrades require a compatibility review for Spring Security, springdoc OpenAPI, Flyway, and test infrastructure.

## Consequences

### Positive

- Demonstrates current Java LTS engineering.
- Uses a mature ecosystem for REST APIs, persistence, security, migrations, and observability.
- The Gradle wrapper makes builds reproducible.
- Java toolchains make the required language level explicit.
- Quality and coverage reports are available from the first executable batch.

### Negative

- Spring Boot 4 is newer than the widely deployed Spring Boot 3 generation.
- Some third-party integrations may lag Spring Boot 4.
- Java 25 may not be installed on older CI images or developer machines.
- Upgrade discipline is required while the ecosystem evolves.

## Guardrails

- Framework versions are pinned.
- Third-party compatibility is checked before upgrades.
- Build and test commands run through the wrapper.
- Database changes are applied only through Flyway.
- Application tests use PostgreSQL rather than silently substituting a different database engine.
- No domain feature depends on preview Java language features.

## Alternatives considered

### Java 21 with Spring Boot 3

A conservative and valid production choice, but rejected because this portfolio project intentionally demonstrates the current Java LTS generation.

### Maven

A valid alternative, but Gradle Kotlin DSL better matches the desired build automation and previous project experience.

### Unpinned globally installed Gradle

Rejected because it produces less reproducible local and CI builds.
