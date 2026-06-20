package com.kanban.service;

import com.kanban.dto.request.CardReorderRequest;
import com.kanban.dto.request.ColumnCreateRequest;
import com.kanban.dto.request.ColumnReorderRequest;
import com.kanban.dto.request.ColumnUpdateRequest;
import com.kanban.dto.response.ColumnResponse;
import com.kanban.entity.BoardColumn;
import com.kanban.entity.Card;
import com.kanban.exception.ConflictException;
import com.kanban.exception.ResourceNotFoundException;
import com.kanban.repository.CardRepository;
import com.kanban.repository.ColumnRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class ColumnService {

    private final ColumnRepository columnRepository;
    private final CardRepository cardRepository;

    public ColumnService(ColumnRepository columnRepository, CardRepository cardRepository) {
        this.columnRepository = columnRepository;
        this.cardRepository = cardRepository;
    }

    @Transactional(readOnly = true)
    public List<ColumnResponse> getColumns() {
        return columnRepository.findAllByOrderByPositionAsc().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ColumnResponse createColumn(ColumnCreateRequest request) {
        int nextPosition = columnRepository.findMaxPosition() + 1;
        BoardColumn column = new BoardColumn();
        column.setName(request.name());
        column.setColor(request.color());
        column.setPosition(nextPosition);
        return toResponse(columnRepository.save(column));
    }

    @Transactional(readOnly = true)
    public ColumnResponse getColumn(Long id) {
        return toResponse(findColumnById(id));
    }

    public ColumnResponse replaceColumn(Long id, ColumnUpdateRequest request) {
        if (request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException("name is required for full update");
        }
        BoardColumn column = findColumnById(id);
        column.setName(request.name());
        column.setColor(request.color());
        return toResponse(columnRepository.save(column));
    }

    public ColumnResponse partialUpdateColumn(Long id, ColumnUpdateRequest request) {
        BoardColumn column = findColumnById(id);
        if (request.name() != null) {
            column.setName(request.name());
        }
        if (request.color() != null) {
            column.setColor(request.color());
        }
        return toResponse(columnRepository.save(column));
    }

    public void deleteColumn(Long id) {
        BoardColumn column = findColumnById(id);
        long cardCount = cardRepository.countByColumnId(id);
        if (cardCount > 0) {
            throw new ConflictException("Cannot delete column with existing cards");
        }
        columnRepository.delete(column);
    }

    public List<ColumnResponse> reorderColumns(ColumnReorderRequest request) {
        List<Long> submittedIds = request.columnIds();
        List<BoardColumn> allColumns = columnRepository.findAllByOrderByPositionAsc();
        Set<Long> existingIds = allColumns.stream()
                .map(BoardColumn::getId)
                .collect(Collectors.toSet());

        if (submittedIds.size() != existingIds.size() ||
                !Set.copyOf(submittedIds).equals(existingIds)) {
            throw new IllegalArgumentException(
                    "column_ids must contain exactly all existing column IDs");
        }

        List<BoardColumn> reordered = new ArrayList<>();
        for (int i = 0; i < submittedIds.size(); i++) {
            Long colId = submittedIds.get(i);
            BoardColumn column = allColumns.stream()
                    .filter(c -> c.getId().equals(colId))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Column not found: " + colId));
            column.setPosition(i);
            reordered.add(columnRepository.save(column));
        }
        return reordered.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<Card> reorderCardsInColumn(Long columnId, CardReorderRequest request) {
        findColumnById(columnId);
        List<Long> submittedIds = request.cardIds();
        List<Card> existingCards = cardRepository.findByColumnIdOrderByPositionAsc(columnId);
        Set<Long> existingIds = existingCards.stream()
                .map(Card::getId)
                .collect(Collectors.toSet());

        if (submittedIds.size() != existingIds.size() ||
                !Set.copyOf(submittedIds).equals(existingIds)) {
            throw new IllegalArgumentException(
                    "card_ids must contain exactly all card IDs in the column");
        }

        List<Card> reordered = new ArrayList<>();
        for (int i = 0; i < submittedIds.size(); i++) {
            Long cardId = submittedIds.get(i);
            Card card = existingCards.stream()
                    .filter(c -> c.getId().equals(cardId))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Card not found: " + cardId));
            card.setPosition(i);
            reordered.add(cardRepository.save(card));
        }
        return reordered;
    }

    private BoardColumn findColumnById(Long id) {
        return columnRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Column not found: " + id));
    }

    private ColumnResponse toResponse(BoardColumn column) {
        return new ColumnResponse(column.getId(), column.getName(), column.getColor(), column.getPosition());
    }
}
