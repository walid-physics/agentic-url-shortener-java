package com.triviola.agentic.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "agentic.ai.enabled=false")
@AutoConfigureMockMvc
class WorkflowApiIntegrationTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    void executesDagPausesForApprovalAndCompletesWithAuditEvidence() throws Exception {
        String createBody = mockMvc.perform(post("/api/workflows")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"requirement":"Build a URL shortener with analytics"}
                            """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.tasks", hasSize(7)))
                .andReturn().getResponse().getContentAsString();
        JsonNode created = objectMapper.readTree(createBody);
        String id = created.path("id").asText();

        mockMvc.perform(post("/api/workflows/{id}/execute", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("WAITING_APPROVAL"))
                .andExpect(jsonPath("$.tasks[3].status").value("WAITING_APPROVAL"));

        mockMvc.perform(post("/api/workflows/{id}/tasks/implementation/approve", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.tasks[6].status").value("PASSED"));

        mockMvc.perform(get("/api/workflows/{id}/events", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(10))))
                .andExpect(jsonPath("$[0].inputHash").isNotEmpty());

        mockMvc.perform(get("/api/workflows/{id}/decisions", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(5))));

        mockMvc.perform(get("/api/workflows/{id}/metrics", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskSuccessRate").value(1.0))
                .andExpect(jsonPath("$.approvalCount").value(1));
    }

}
