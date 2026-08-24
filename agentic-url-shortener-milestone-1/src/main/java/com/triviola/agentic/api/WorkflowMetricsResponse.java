package com.triviola.agentic.api;

import com.triviola.agentic.workflow.WorkflowStatus;

public record WorkflowMetricsResponse(WorkflowStatus status, double taskSuccessRate,
                                      int retryCount, int rollbackCount, int approvalCount,
                                      long endToEndLatencyMs, Long meanTimeToRecoveryMs) {}
