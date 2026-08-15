package com.samharrison.incidentresponse.feedback;

import java.time.Instant;

public record FeedbackQueryCriteria(String policyVersion, Instant from, Instant to, int limit) {
  public FeedbackQueryCriteria {
    if (limit < 1 || limit > 50) {
      throw new IllegalArgumentException("limit must be between 1 and 50");
    }
    if (from != null && to != null && to.isBefore(from)) {
      throw new IllegalArgumentException("feedback query window is invalid");
    }
    if (policyVersion != null && (policyVersion.isBlank() || policyVersion.length() > 64)) {
      throw new IllegalArgumentException("policyVersion is outside the permitted range");
    }
  }

  public static FeedbackQueryCriteria defaults() {
    return new FeedbackQueryCriteria(null, null, null, 50);
  }
}
