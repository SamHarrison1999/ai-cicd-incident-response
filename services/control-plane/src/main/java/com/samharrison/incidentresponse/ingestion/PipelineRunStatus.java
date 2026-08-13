package com.samharrison.incidentresponse.ingestion;

public enum PipelineRunStatus {
  QUEUED,
  RUNNING,
  SUCCEEDED,
  FAILED,
  CANCELLED,
  SKIPPED,
  TIMED_OUT,
  UNKNOWN
}
