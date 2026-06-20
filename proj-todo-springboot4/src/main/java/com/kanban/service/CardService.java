package com.kanban.service;

import com.kanban.dto.request.CardCreateRequest;
import com.kanban.dto.request.CardMoveRequest;
import com.kanban.dto.request.CardUpdateRequest;
import com.kanban.dto.response.CardResponse;
import com.kanban.entity.BoardColumn;
import com.kanban.entity.Card;
import com.kanban.exception.ResourceNotFoundException;
import com.kanban.repository.CardRepository;
import com.kanban.repository.ColumnRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CardService {

    private final CardRepository cardRepository;
    private final ColumnRepository columnRepository;

    public CardService(CardRepository cardRepository, ColumnRepository columnRepository) {
        this.cardRepository = cardRepository;
        this.columnRepository = columnRepository;
    }

    @Transactional(readOnly = true)
    public List<CardResponse> getCards(Long columnId) {
        List<Card> cards;
        if (columnId != null) {
            cards = cardRepository.findByColumnIdOrderByPositionAsc(columnId);
        } else {
            cards = cardRepository.findAllOrderedByColumnAndPosition();
        }
        return cards.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public CardResponse createCard(CardCreateRequest request) {
        BoardColumn column = findColumnById(request.columnId());
        int nextPosition = cardRepository.findMaxPositionInColumn(request.columnId()) + 1;

        Card card = new Card();
        card.setColumn(column);
        card.setTitle(request.title());
        card.setDescription(request.description());
        if (request.priority() != null) {
            card.setPriority(Card.Priority.valueOf(request.priority().toUpperCase()));
        }
        card.setPosition(nextPosition);
        return toResponse(cardRepository.save(card));
    }

    @Transactional(readOnly = true)
    public CardResponse getCard(Long id) {
        return toResponse(findCardById(id));
    }

    public CardResponse replaceCard(Long id, CardUpdateRequest request) {
        if (request.title() == null || request.title().isBlank()) {
            throw new IllegalArgumentException("title is required for full update");
        }
        Card card = findCardById(id);
        card.setTitle(request.title());
        card.setDescription(request.description());
        card.setPriority(request.priority() != null ? Card.Priority.valueOf(request.priority().toUpperCase()) : null);
        if (request.columnId() != null) {
            card.setColumn(findColumnById(request.columnId()));
        }
        if (request.position() != null) {
            card.setPosition(request.position());
        }
        return toResponse(cardRepository.save(card));
    }

    public CardResponse partialUpdateCard(Long id, CardUpdateRequest request) {
        Card card = findCardById(id);
        if (request.title() != null) {
            card.setTitle(request.title());
        }
        if (request.description() != null) {
            card.setDescription(request.description());
        }
        if (request.priority() != null) {
            card.setPriority(Card.Priority.valueOf(request.priority().toUpperCase()));
        }
        if (request.columnId() != null) {
            card.setColumn(findColumnById(request.columnId()));
        }
        if (request.position() != null) {
            card.setPosition(request.position());
        }
        return toResponse(cardRepository.save(card));
    }

    public void deleteCard(Long id) {
        Card card = findCardById(id);
        Long columnId = card.getColumn().getId();
        int deletedPosition = card.getPosition();
        cardRepository.delete(card);
        cardRepository.decrementPositionsAfterInColumn(columnId, deletedPosition);
    }

    public CardResponse moveCard(Long id, CardMoveRequest request) {
        Card card = findCardById(id);
        Long sourceColumnId = card.getColumn().getId();
        int sourcePosition = card.getPosition();
        Long targetColumnId = request.columnId();
        int targetPosition = request.position();

        if (sourceColumnId.equals(targetColumnId)) {
            if (sourcePosition == targetPosition) {
                return toResponse(card);
            }
            if (targetPosition > sourcePosition) {
                cardRepository.shiftDownBetween(sourceColumnId, sourcePosition + 1, targetPosition);
            } else {
                cardRepository.shiftUpBetween(sourceColumnId, targetPosition, sourcePosition - 1);
            }
            card.setPosition(targetPosition);
        } else {
            findColumnById(targetColumnId);
            cardRepository.decrementPositionsAfterInColumn(sourceColumnId, sourcePosition);
            cardRepository.incrementPositionsFromInColumn(targetColumnId, targetPosition);
            card.setColumn(findColumnById(targetColumnId));
            card.setPosition(targetPosition);
        }

        return toResponse(cardRepository.save(card));
    }

    private Card findCardById(Long id) {
        return cardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found: " + id));
    }

    private BoardColumn findColumnById(Long id) {
        return columnRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Column not found: " + id));
    }

    private CardResponse toResponse(Card card) {
        return new CardResponse(
                card.getId(),
                card.getColumn().getId(),
                card.getTitle(),
                card.getDescription(),
                card.getPriority() != null ? card.getPriority().name() : null,
                card.getPosition(),
                card.getCreatedAt(),
                card.getUpdatedAt()
        );
    }
}
