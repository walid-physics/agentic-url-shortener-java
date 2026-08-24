package com.triviola.agentic.orchestrator;

import com.triviola.agentic.workflow.RiskLevel;
import org.springframework.stereotype.Component;

@Component
public class PolicyEngine {
    public boolean requiresApproval(RiskLevel riskLevel) {
        return riskLevel == RiskLevel.HIGH || riskLevel == RiskLevel.CRITICAL;
    }
}
