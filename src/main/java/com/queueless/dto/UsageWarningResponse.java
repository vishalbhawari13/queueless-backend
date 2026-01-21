package com.queueless.dto;

public record UsageWarningResponse(
        int used,
        int limit,
        int percent,
        boolean warning
) {}
