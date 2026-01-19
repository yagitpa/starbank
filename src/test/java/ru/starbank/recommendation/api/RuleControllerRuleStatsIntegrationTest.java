package ru.starbank.recommendation.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Stage 3: интеграционный тест эндпойнта GET /rule/stats (RuleController).
 *
 * Проверяем контракт:
 * - после создания динамических правил через POST /rule
 * - GET /rule/stats возвращает ВСЕ правила
 * - для новых правил count = "0"
 */
@SpringBootTest(properties = "telegram.bot.enabled=false")
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class RuleControllerRuleStatsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getRuleStats_shouldReturnAllRulesIncludingZeroCounts() throws Exception {
        long id1 = createRule("Rule Product 1", UUID.randomUUID().toString(), "Text 1");
        long id2 = createRule("Rule Product 2", UUID.randomUUID().toString(), "Text 2");

        mockMvc.perform(get("/rule/stats"))
               .andExpect(status().isOk())
               .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))

               // rule_id присутствуют в массиве stats
               .andExpect(jsonPath("$.stats[*].rule_id", hasItem(String.valueOf(id1))))
               .andExpect(jsonPath("$.stats[*].rule_id", hasItem(String.valueOf(id2))))

               // у новых правил count = "0"
               .andExpect(jsonPath("$.stats[?(@.rule_id == '" + id1 + "')].count").value(hasItem("0")))
               .andExpect(jsonPath("$.stats[?(@.rule_id == '" + id2 + "')].count").value(hasItem("0")));
    }

    private long createRule(String productName, String productId, String productText) throws Exception {
        // Минимально валидное правило: USER_OF DEBIT (negate=false)
        Map<String, Object> body = Map.of(
                "product_name", productName,
                "product_id", productId,
                "product_text", productText,
                "rule", List.of(
                        Map.of(
                                "query", "USER_OF",
                                "arguments", List.of("DEBIT"),
                                "negate", false
                        )
                )
        );

        String json = objectMapper.writeValueAsString(body);

        String responseJson = mockMvc.perform(post("/rule")
                                             .contentType(MediaType.APPLICATION_JSON)
                                             .content(json))
                                     .andExpect(status().isOk())
                                     .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                                     .andExpect(jsonPath("$.id").exists())
                                     .andReturn()
                                     .getResponse()
                                     .getContentAsString();

        Map<?, ?> resp = objectMapper.readValue(responseJson, Map.class);
        Object idObj = resp.get("id");

        if (idObj instanceof Number n) {
            return n.longValue();
        }
        return Long.parseLong(String.valueOf(idObj));
    }
}