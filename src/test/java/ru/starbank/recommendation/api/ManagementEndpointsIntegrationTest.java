package ru.starbank.recommendation.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Stage 3: интеграционные тесты Management API.
 *
 * В проекте management разделён на два контроллера:
 * - CacheManagementController: POST /management/clear-caches
 * - ManagementInfoController: GET /management/info
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ManagementEndpointsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * Мокаем BuildProperties, чтобы тест не зависел от build-info.properties.
     */
    @MockitoBean
    private BuildProperties buildProperties;

    @Test
    void clearCaches_shouldReturnOk() throws Exception {
        mockMvc.perform(post("/management/clear-caches"))
               .andExpect(status().isOk())
               .andExpect(content().string(""));
    }

    @Test
    void info_shouldReturnNameAndVersion() throws Exception {
        when(buildProperties.getName()).thenReturn("starbank-recommendation");
        when(buildProperties.getVersion()).thenReturn("test");

        mockMvc.perform(get("/management/info"))
               .andExpect(status().isOk())
               .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$.name").value("starbank-recommendation"))
               .andExpect(jsonPath("$.version").value("test"));
    }
}