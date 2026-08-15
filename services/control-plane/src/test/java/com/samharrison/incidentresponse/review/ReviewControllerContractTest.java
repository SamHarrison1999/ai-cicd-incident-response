package com.samharrison.incidentresponse.review;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class ReviewControllerContractTest {
  @Test
  void reviewRequestCarriesBoundedGovernanceFields() {
    ReviewController.ReviewRequest request =
        new ReviewController.ReviewRequest(
            ReviewAction.EDIT,
            ReviewReason.INCORRECT_SCOPE,
            "adjust scope",
            "bounded summary",
            "bounded cause");
    assertThat(request.action()).isEqualTo(ReviewAction.EDIT);
    assertThat(request.editedSummary()).isEqualTo("bounded summary");
  }

  @Test
  void resolutionRequestCarriesReviewedVersionReference() {
    UUID version = UUID.randomUUID();
    ResolutionController.ResolutionRequest request =
        new ResolutionController.ResolutionRequest(
            UUID.randomUUID(), version, "documented resolution");
    assertThat(request.reviewedVersionId()).isEqualTo(version);
  }
}
