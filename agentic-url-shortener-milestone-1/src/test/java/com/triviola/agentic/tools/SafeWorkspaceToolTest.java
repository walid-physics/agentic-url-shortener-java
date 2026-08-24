package com.triviola.agentic.tools;

import com.triviola.agentic.agent.output.FileChange;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

class SafeWorkspaceToolTest {
    private final SafeWorkspaceTool tool = new SafeWorkspaceTool();

    @Test
    void rejectsPathTraversal() {
        assertThrows(SecurityException.class, () -> tool.applyUpserts(List.of(
                new FileChange("../../outside.txt", "UPSERT", "unsafe", "test"))));
    }

    @Test
    void rejectsDeleteOperations() {
        assertThrows(SecurityException.class, () -> tool.applyUpserts(List.of(
                new FileChange("safe.txt", "DELETE", "", "test"))));
    }
}
