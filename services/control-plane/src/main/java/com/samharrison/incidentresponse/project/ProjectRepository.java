package com.samharrison.incidentresponse.project;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

  Optional<Project> findByIdAndOrganisationId(UUID projectId, UUID organisationId);

  Optional<Project> findByOrganisationIdAndSlug(UUID organisationId, String slug);

  boolean existsByOrganisationIdAndSlug(UUID organisationId, String slug);

  List<Project> findAllByOrganisationIdOrderByNameAsc(UUID organisationId);
}
