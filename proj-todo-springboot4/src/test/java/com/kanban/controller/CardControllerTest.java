package com.kanban.controller;

import com.kanban.dto.request.CardCreateRequest;
import com.kanban.dto.request.CardMoveRequest;
import com.kanban.dto.request.CardUpdateRequest;
import com.kanban.dto.response.CardResponse;
import com.kanban.exception.GlobalExceptionHandler;
import com.kanban.exception.ResourceNotFoundException;
import com.kanban.service.CardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CardControllerTest {

    private MockMvc mockMvc;
    private JsonMapper jsonMapper;

    @Mock
    private CardService cardService;

    @BeforeEach
    void setUp() {
        jsonMapper = JsonMapper.builder()
                .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .build();
        JacksonJsonHttpMessageConverter converter = new JacksonJsonHttpMessageConverter(jsonMapper);
        CardController controller = new CardController(cardService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(converter)
                .build();
    }

    private CardResponse sampleCard() {
        return new CardResponse(10L, 1L, "Task 1", "desc", "LOW", 0,
                LocalDateTime.of(2024, 1, 1, 0, 0),
                LocalDateTime.of(2024, 1, 2, 0, 0));
    }

    @Test
    void getCards_allCards_returns200() throws Exception {
        when(cardService.getCards(null)).thenReturn(List.of(sampleCard()));

        mockMvc.perform(get("/cards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Task 1"));
    }

    @Test
    void getCards_filteredByColumn_returns200() throws Exception {
        when(cardService.getCards(1L)).thenReturn(List.of(sampleCard()));

        mockMvc.perform(get("/cards").param("column_id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].column_id").value(1));
    }

    @Test
    void createCard_returns201() throws Exception {
        when(cardService.createCard(any())).thenReturn(sampleCard());

        mockMvc.perform(post("/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(
                                new CardCreateRequest(1L, "Task 1", "desc", "LOW"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void createCard_returns422WhenTitleBlank() throws Exception {
        mockMvc.perform(post("/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"column_id\":1,\"title\":\"\"}"))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void createCard_returns422WhenColumnIdNull() throws Exception {
        mockMvc.perform(post("/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Task\"}"))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void getCard_returns200() throws Exception {
        when(cardService.getCard(10L)).thenReturn(sampleCard());

        mockMvc.perform(get("/cards/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void getCard_returns404WhenNotFound() throws Exception {
        when(cardService.getCard(99L)).thenThrow(new ResourceNotFoundException("Card not found: 99"));

        mockMvc.perform(get("/cards/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Card not found: 99"));
    }

    @Test
    void replaceCard_returns200() throws Exception {
        when(cardService.replaceCard(anyLong(), any())).thenReturn(sampleCard());

        mockMvc.perform(put("/cards/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(
                                new CardUpdateRequest("Task 1", "desc", "LOW", 1L, 0))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Task 1"));
    }

    @Test
    void replaceCard_returns422WhenServiceThrows() throws Exception {
        when(cardService.replaceCard(anyLong(), any()))
                .thenThrow(new IllegalArgumentException("title is required"));

        mockMvc.perform(put("/cards/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(
                                new CardUpdateRequest(null, null, null, null, null))))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void partialUpdateCard_returns200() throws Exception {
        when(cardService.partialUpdateCard(anyLong(), any())).thenReturn(sampleCard());

        mockMvc.perform(patch("/cards/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"updated\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteCard_returns204() throws Exception {
        doNothing().when(cardService).deleteCard(10L);

        mockMvc.perform(delete("/cards/10"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteCard_returns404WhenNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Card not found: 99"))
                .when(cardService).deleteCard(99L);

        mockMvc.perform(delete("/cards/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void moveCard_returns200() throws Exception {
        when(cardService.moveCard(anyLong(), any())).thenReturn(
                new CardResponse(10L, 2L, "Task 1", "desc", "LOW", 0,
                        LocalDateTime.of(2024, 1, 1, 0, 0),
                        LocalDateTime.of(2024, 1, 2, 0, 0)));

        mockMvc.perform(patch("/cards/10/move")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(new CardMoveRequest(2L, 0))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.column_id").value(2));
    }

    @Test
    void moveCard_returns422WhenColumnIdNull() throws Exception {
        mockMvc.perform(patch("/cards/10/move")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"position\":0}"))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void moveCard_returns422WhenPositionNegative() throws Exception {
        mockMvc.perform(patch("/cards/10/move")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"column_id\":1,\"position\":-1}"))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void moveCard_returns404WhenCardNotFound() throws Exception {
        when(cardService.moveCard(anyLong(), any()))
                .thenThrow(new ResourceNotFoundException("Card not found: 99"));

        mockMvc.perform(patch("/cards/99/move")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(new CardMoveRequest(1L, 0))))
                .andExpect(status().isNotFound());
    }
}
