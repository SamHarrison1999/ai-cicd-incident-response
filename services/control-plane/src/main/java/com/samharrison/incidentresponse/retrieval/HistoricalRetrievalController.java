package com.samharrison.incidentresponse.retrieval;

import com.samharrison.incidentresponse.tenancy.CurrentUserProvider;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/organisations/{organisationId}/projects/{projectId}/historical-retrieval")
public class HistoricalRetrievalController {

  private final HistoricalRetrievalService service;
  private final CurrentUserProvider currentUserProvider;

  public HistoricalRetrievalController(
      HistoricalRetrievalService service, CurrentUserProvider currentUserProvider) {
    this.service = service;
    this.currentUserProvider = currentUserProvider;
  }

  @GetMapping
  HistoricalRetrievalResponse search(
      Authentication authentication,
      @PathVariable UUID organisationId,
      @PathVariable UUID projectId,
      @RequestParam(required = false) String diagnosisCategory,
      @RequestParam(required = false) String provider,
      @RequestParam(required = false) String pipeline,
      @RequestParam(required = false) String environment,
      @RequestParam(required = false) String branch,
      @RequestParam(required = false) String commitSha,
      @RequestParam(required = false) String from,
      @RequestParam(required = false) String to,
      @RequestParam(required = false, name = "q") String query,
      @RequestParam(required = false) String cursor,
      @RequestParam(defaultValue = "25") int limit) {
    HistoricalRetrievalService.HistoricalRetrievalPage page =
        service.search(
            currentUserProvider.requireUserId(authentication),
            organisationId,
            projectId,
            diagnosisCategory,
            provider,
            pipeline,
            environment,
            branch,
            commitSha,
            from,
            to,
            query,
            cursor,
            limit);
    return new HistoricalRetrievalResponse(page.items(), page.nextCursor(), page.hasNext());
  }

  public record HistoricalRetrievalResponse(
      List<HistoricalRetrievalService.HistoricalRetrievalItem> items,
      String nextCursor,
      boolean hasNext) {}
}
