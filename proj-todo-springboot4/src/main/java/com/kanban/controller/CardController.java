package com.kanban.controller;

import com.kanban.dto.request.CardCreateRequest;
import com.kanban.dto.request.CardMoveRequest;
import com.kanban.dto.request.CardUpdateRequest;
import com.kanban.dto.response.CardResponse;
import com.kanban.service.CardService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cards")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @GetMapping
    public ResponseEntity<List<CardResponse>> getCards(
            @RequestParam(name = "column_id", required = false) Long columnId) {
        return ResponseEntity.ok(cardService.getCards(columnId));
    }

    @PostMapping
    public ResponseEntity<CardResponse> createCard(@Valid @RequestBody CardCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cardService.createCard(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CardResponse> getCard(@PathVariable Long id) {
        return ResponseEntity.ok(cardService.getCard(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CardResponse> replaceCard(@PathVariable Long id,
                                                    @Valid @RequestBody CardUpdateRequest request) {
        return ResponseEntity.ok(cardService.replaceCard(id, request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CardResponse> partialUpdateCard(@PathVariable Long id,
                                                          @Valid @RequestBody CardUpdateRequest request) {
        return ResponseEntity.ok(cardService.partialUpdateCard(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        cardService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/move")
    public ResponseEntity<CardResponse> moveCard(@PathVariable Long id,
                                                 @Valid @RequestBody CardMoveRequest request) {
        return ResponseEntity.ok(cardService.moveCard(id, request));
    }
}
