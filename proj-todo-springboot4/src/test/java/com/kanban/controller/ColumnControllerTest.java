package com.kanban.controller;

import com.kanban.dto.request.CardReorderRequest;
import com.kanban.dto.request.ColumnCreateRequest;
import com.kanban.dto.request.ColumnReorderRequest;
import com.kanban.dto.request.ColumnUpdateRequest;
import com.kanban.dto.response.ColumnResponse;
import com.kanban.entity.BoardColumn;
import com.kanban.entity.Card;
import com.kanban.exception.ConflictException;
import com.kanban.exception.GlobalExceptionHandler;
import com.kanban.exception.ResourceNotFoundException;
import com.kanban.service.CardService;
import com.kanban.service.ColumnService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ColumnControllerTest {

    private MockMvc mockMvc;
    private JsonMapper jsonMapper;

    @Mock
    private ColumnService columnService;

    @Mock
    private CardService cardService;

    @BeforeEach
    void setUp() {
        jsonMapper = JsonMapper.builder()
                .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .build();
        JacksonJsonHttpMessageConverter converter = new JacksonJsonHttpMessageConverter(jsonMapper);
        ColumnController controller = new ColumnController(columnService, cardService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(converter)
                .build();
    }

    @Test
    void getColumns_returns200() throws Exception {
        when(columnService.getColumns()).thenReturn(List.of(
                new ColumnResponse(1L, "Todo", "#FF0000", 0),
                new ColumnResponse(2L, "Done", "#00FF00", 1)
        ));

        mockMvc.perform(get("/columns"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Todo"))
                .andExpect(jsonPath("$[1].name").value("Done"));
    }

    @Test
    void createColumn_returns201() throws Exception {
        when(columnService.createColumn(any())).thenReturn(
                new ColumnResponse(1L, "Backlog", null, 0));

        mockMvc.perform(post("/columns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(new ColumnCreateRequest("Backlog", null))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Backlog"));
    }

    @Test
    void createColumn_returns422WhenNameBlank() throws Exception {
        mockMvc.perform(post("/columns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void getColumn_returns200() throws Exception {
        when(columnService.getColumn(1L)).thenReturn(new ColumnResponse(1L, "Todo", "#FF0000", 0));

        mockMvc.perform(get("/columns/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getColumn_returns404WhenNotFound() throws Exception {
        when(columnService.getColumn(99L)).thenThrow(new ResourceNotFoundException("Column not found: 99"));

        mockMvc.perform(get("/columns/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Column not found: 99"));
    }

    @Test
    void replaceColumn_returns200() throws Exception {
        when(columnService.replaceColumn(anyLong(), any())).thenReturn(
                new ColumnResponse(1L, "Updated", "#0000FF", 0));

        mockMvc.perform(put("/columns/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(new ColumnUpdateRequest("Updated", "#0000FF"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    void replaceColumn_returns422WhenServiceThrows() throws Exception {
        when(columnService.replaceColumn(anyLong(), any()))
                .thenThrow(new IllegalArgumentException("name is required"));

        mockMvc.perform(put("/columns/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(new ColumnUpdateRequest(null, null))))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void partialUpdateColumn_returns200() throws Exception {
        when(columnService.partialUpdateColumn(anyLong(), any())).thenReturn(
                new ColumnResponse(1L, "Todo", "#AABBCC", 0));

        mockMvc.perform(patch("/columns/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"color\":\"#AABBCC\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.color").value("#AABBCC"));
    }

    @Test
    void deleteColumn_returns204() throws Exception {
        doNothing().when(columnService).deleteColumn(1L);

        mockMvc.perform(delete("/columns/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteColumn_returns409WhenCardsExist() throws Exception {
        doThrow(new ConflictException("Cannot delete column with existing cards"))
                .when(columnService).deleteColumn(1L);

        mockMvc.perform(delete("/columns/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Cannot delete column with existing cards"));
    }

    @Test
    void deleteColumn_returns404WhenNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Column not found: 1"))
                .when(columnService).deleteColumn(1L);

        mockMvc.perform(delete("/columns/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void reorderColumns_returns200() throws Exception {
        when(columnService.reorderColumns(any())).thenReturn(List.of(
                new ColumnResponse(2L, "Done", null, 0),
                new ColumnResponse(1L, "Todo", null, 1)
        ));

        mockMvc.perform(patch("/columns/reorder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(new ColumnReorderRequest(List.of(2L, 1L)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2));
    }

    @Test
    void reorderColumns_returns422WhenInvalidIds() throws Exception {
        when(columnService.reorderColumns(any()))
                .thenThrow(new IllegalArgumentException("column_ids must contain exactly all existing column IDs"));

        mockMvc.perform(patch("/columns/reorder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(new ColumnReorderRequest(List.of(1L)))))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void getCardsInColumn_returns200() throws Exception {
        when(cardService.getCards(1L)).thenReturn(List.of());

        mockMvc.perform(get("/columns/1/cards"))
                .andExpect(status().isOk());
    }

    @Test
    void reorderCardsInColumn_returns200() throws Exception {
        BoardColumn col = new BoardColumn();
        col.setId(1L);
        col.setName("Todo");
        col.setPosition(0);

        Card c1 = new Card();
        c1.setId(10L);
        c1.setColumn(col);
        c1.setTitle("Card A");
        c1.setPosition(0);

        when(columnService.reorderCardsInColumn(anyLong(), any())).thenReturn(List.of(c1));

        mockMvc.perform(patch("/columns/1/cards/reorder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(new CardReorderRequest(List.of(10L)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10));
    }
}
