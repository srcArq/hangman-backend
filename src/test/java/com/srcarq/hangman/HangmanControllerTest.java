package com.srcarq.hangman;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class HangmanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private JsonNode selectLevel(int level) throws Exception {
        String response = mockMvc.perform(post("/api/selectLevel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"level\":" + level + "}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response);
    }

    private String checkLetterBody(String token, String letter) throws Exception {
        return objectMapper.writeValueAsString(Map.of("token", token, "letter", letter));
    }

    @Test
    void selectLevelStartsGameAndDoesNotLeakWord() throws Exception {
        JsonNode body = selectLevel(1);

        assertThat(body.get("attempts").asInt()).isEqualTo(10);
        assertThat(body.get("status").asText()).isEqualTo("IN_PROGRESS");
        assertThat(body.get("token").asText()).isNotBlank();
        assertThat(body.get("wordLength").asInt()).isGreaterThan(0);
        assertThat(body.get("resultWord").isNull()).isTrue();
        // La palabra NUNCA viaja en claro al iniciar.
        assertThat(body.has("selectedWord")).isFalse();
        body.get("maskedWord").forEach(node -> assertThat(node.asText()).isEqualTo("_"));
    }

    @Test
    void fullGameFlowEndsAndRevealsWordOnlyAtEnd() throws Exception {
        String token = selectLevel(1).get("token").asText();

        String status = "IN_PROGRESS";
        String resultWord = null;
        for (String letter : "ABCDEFGHIJKLMNÑOPQRSTUVWXYZ".split("")) {
            JsonNode body = objectMapper.readTree(mockMvc.perform(post("/api/checkLetter")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(checkLetterBody(token, letter)))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString());

            token = body.get("token").asText();
            status = body.get("status").asText();
            if (status.equals("IN_PROGRESS")) {
                assertThat(body.get("resultWord").isNull()).isTrue();
            } else {
                resultWord = body.get("resultWord").asText();
                break;
            }
        }

        assertThat(status).isIn("WON", "LOST");
        assertThat(resultWord).isNotBlank();

        // Jugar tras el fin de partida se rechaza con 409.
        mockMvc.perform(post("/api/checkLetter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(checkLetterBody(token, "A")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("GAME_OVER"));
    }

    @Test
    void invalidLevelReturns400() throws Exception {
        mockMvc.perform(post("/api/selectLevel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"level\":9}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_INPUT"));
    }

    @Test
    void invalidLetterReturns400() throws Exception {
        String token = selectLevel(1).get("token").asText();
        mockMvc.perform(post("/api/checkLetter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(checkLetterBody(token, "AB")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_INPUT"));
    }

    @Test
    void invalidTokenReturns400() throws Exception {
        mockMvc.perform(post("/api/checkLetter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(checkLetterBody("garbage-token", "A")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_TOKEN"));
    }

    @Test
    void acceptsEnyeLetterUpperAndLowercase() throws Exception {
        String token = selectLevel(1).get("token").asText();
        mockMvc.perform(post("/api/checkLetter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(checkLetterBody(token, "Ñ")))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/checkLetter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(checkLetterBody(token, "ñ")))
                .andExpect(status().isOk());
    }
}
