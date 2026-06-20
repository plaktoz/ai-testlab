package com.kanban.controller;

import com.kanban.dto.request.CardReorderRequest;
import com.kanban.dto.request.ColumnCreateRequest;
import com.kanban.dto.request.ColumnReorderRequest;
import com.kanban.dto.request.ColumnUpdateRequest;
import com.kanban.dto.response.CardResponse;
import com.kanban.dto.response.ColumnResponse;
import com.kanban.entity.Card;
import com.kanban.service.CardService;
import com.kanban.service.ColumnService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/columns")
public class ColumnController {

    private final ColumnService columnService;
    private final CardService cardService;

    public ColumnController(ColumnService columnService, CardService cardService) {
        this.columnService = columnService;
        this.cardService = cardService;
    }

    @GetMapping
    public ResponseEntity<List<ColumnResponse>> getColumns() {
        return ResponseEntity.ok(columnService.getColumns());
    }

    @PostMapping
    public ResponseEntity<ColumnResponse> createColumn(@Valid @RequestBody ColumnCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(columnService.createColumn(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ColumnResponse> getColumn(@PathVariable Long id) {
        return ResponseEntity.ok(columnService.getColumn(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ColumnResponse> replaceColumn(@PathVariable Long id,
                                                        @Valid @RequestBody ColumnUpdateRequest request) {
        return ResponseEntity.ok(columnService.replaceColumn(id, request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ColumnResponse> partialUpdateColumn(@PathVariable Long id,
                                                              @Valid @RequestBody ColumnUpdateRequest request) {
        return ResponseEntity.ok(columnService.partialUpdateColumn(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteColumn(@PathVariable Long id) {
        columnService.deleteColumn(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/reorder")
    public ResponseEntity<List<ColumnResponse>> reorderColumns(@Valid @RequestBody ColumnReorderRequest request) {
        return ResponseEntity.ok(columnService.reorderColumns(request));
    }

    @GetMapping("/{columnId}/cards")
    public ResponseEntity<List<CardResponse>> getCardsInColumn(@PathVariable Long columnId) {
        return ResponseEntity.ok(cardService.getCards(columnId));
    }

    @PatchMapping("/{columnId}/cards/reorder")
    public ResponseEntity<List<CardResponse>> reorderCardsInColumn(@PathVariable Long columnId,
                                                                    @Valid @RequestBody CardReorderRequest request) {
        List<Card> cards = columnService.reorderCardsInColumn(columnId, request);
        List<CardResponse> response = cards.stream()
                .map(c -> new CardResponse(
                        c.getId(),
                        c.getColumn().getId(),
                        c.getTitle(),
                        c.getDescription(),
                        c.getPriority() != null ? c.getPriority().name() : null,
                        c.getPosition(),
                        c.getCreatedAt(),
                        c.getUpdatedAt()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}
