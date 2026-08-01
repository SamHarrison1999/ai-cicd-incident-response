package com.samharrison.incidentresponse.system;

import java.time.Instant;

public record SystemStatusResponse(
    String service, String version, String status, Instant timestamp) {}
