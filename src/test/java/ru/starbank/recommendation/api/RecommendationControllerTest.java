package ru.starbank.recommendation.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.starbank.recommendation.api.recommendapi.RecommendationController;
import ru.starbank.recommendation.domain.dto.RecommendationDto;
import ru.starbank.recommendation.domain.dto.RecommendationResponseDto;
import ru.starbank.recommendation.service.RecommendationService;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(controllers = RecommendationController.class)
class RecommendationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RecommendationService recommendationService;

    @Test
    void shouldReturn200AndUserIdFieldAsUser_id() throws Exception {
        UUID userId = UUID.randomUUID();
        RecommendationResponseDto dto = new RecommendationResponseDto(
                userId,
                List.of(new RecommendationDto(UUID.randomUUID(), "X", "Y"))
        );

        when(recommendationService.getRecommendations(userId)).thenReturn(dto);

        mockMvc.perform(get("/recommendation/{user_id}", userId))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.user_id").value(userId.toString()))
               .andExpect(jsonPath("$.recommendations").isArray());
    }

    @Test
    void shouldReturn400OnInvalidUuid() throws Exception {
        mockMvc.perform(get("/recommendation/{user_id}", "not-a-uuid"))
               .andExpect(status().isBadRequest());
    }
}