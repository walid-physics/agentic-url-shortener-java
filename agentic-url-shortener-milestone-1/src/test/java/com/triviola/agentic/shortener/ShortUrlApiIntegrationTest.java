package com.triviola.agentic.shortener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ShortUrlApiIntegrationTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired ShortUrlRepository repository;

    @Test
    void createsRedirectsAndTracksAnalytics() throws Exception {
        String body = mockMvc.perform(post("/api/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"originalUrl\":\"https://example.com/docs\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").isNotEmpty())
                .andReturn().getResponse().getContentAsString();
        JsonNode created = objectMapper.readTree(body);
        String code = created.path("code").asText();

        mockMvc.perform(get("/r/{code}", code))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://example.com/docs"));

        mockMvc.perform(get("/api/urls/{code}/analytics", code))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clickCount").value(1));

        mockMvc.perform(delete("/api/urls/{code}", code)).andExpect(status().isNoContent());
        mockMvc.perform(get("/r/{code}", code)).andExpect(status().isNotFound());
    }

    @Test
    void rejectsDangerousSchemes() throws Exception {
        mockMvc.perform(post("/api/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"originalUrl\":\"javascript:alert(1)\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void expiredUrlReturnsGone() throws Exception {
        repository.save(new ShortUrlEntity(UUID.randomUUID(), "Expired1", "https://example.com",
                Instant.now().minusSeconds(1)));

        mockMvc.perform(get("/r/Expired1")).andExpect(status().isGone());
    }
}
