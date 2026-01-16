package ru.starbank.recommendation.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Интеграционный тест Stage 2:
 * CRUD динамических правил + влияние на /recommendation/{userId}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class Stage2DynamicRulesIntegrationTest {

    private static final String USER_ID = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void dynamicRuleCrud_andRecommendationFlow_shouldWork() throws Exception {
        // 1) POST /rule -> create
        String createJson = """
            {
              "product_name": "Dynamic Debit Product",
              "product_id": "99999999-9999-9999-9999-999999999999",
              "product_text": "Shown when USER_OF DEBIT",
              "rule": [
                { "query": "USER_OF", "arguments": ["DEBIT"], "negate": false }
              ]
            }
            """;

        String created = mockMvc.perform(post("/rule")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(createJson))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id", notNullValue()))
                                .andExpect(jsonPath("$.product_name").value("Dynamic Debit Product"))
                                .andExpect(jsonPath("$.product_id").value("99999999-9999-9999-9999-999999999999"))
                                .andExpect(jsonPath("$.product_text").value("Shown when USER_OF DEBIT"))
                                .andExpect(jsonPath("$.rule", hasSize(1)))
                                .andReturn()
                                .getResponse()
                                .getContentAsString();

        long ruleId = objectMapper.readTree(created).get("id").asLong();

        // 2) GET /rule -> list contains
        mockMvc.perform(get("/rule"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.data", not(empty())))
               .andExpect(jsonPath("$.data[*].id", hasItem((int) ruleId)));

        // 3) GET /recommendation/{userId} -> contains dynamic product
        mockMvc.perform(get("/recommendation/{userId}", USER_ID))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.user_id").value(USER_ID))
               .andExpect(jsonPath("$.recommendations[*].id",
                       hasItem("99999999-9999-9999-9999-999999999999")))
               .andExpect(jsonPath("$.recommendations[*].name",
                       hasItem("Dynamic Debit Product")));

        // 4) DELETE /rule/{id} -> 204
        mockMvc.perform(delete("/rule/{id}", ruleId))
               .andExpect(status().isNoContent());

        // 5) GET /rule -> no longer contains
        mockMvc.perform(get("/rule"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.data[*].id", not(hasItem((int) ruleId))));
    }
}