package com.triviola.agentic.agent.output;

import java.util.List;

public record PlannedTask(String id, String description, String agent, List<String> dependencies,
                          String risk, boolean approvalRequired) {}
