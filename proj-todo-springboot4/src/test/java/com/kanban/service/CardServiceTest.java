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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private ColumnRepository columnRepository;

    @InjectMocks
    private CardService cardService;

    private BoardColumn col1;
    private BoardColumn col2;
    private Card card1;

    @BeforeEach
    void setUp() {
        col1 = new BoardColumn();
        col1.setId(1L);
        col1.setName("Todo");
        col1.setPosition(0);

        col2 = new BoardColumn();
        col2.setId(2L);
        col2.setName("Done");
        col2.setPosition(1);

        card1 = new Card();
        card1.setId(10L);
        card1.setColumn(col1);
        card1.setTitle("Task 1");
        card1.setDescription("desc");
        card1.setPriority(Card.Priority.LOW);
        card1.setPosition(0);
        card1.setCreatedAt(LocalDateTime.now());
        card1.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void getCards_allCards() {
        when(cardRepository.findAllOrderedByColumnAndPosition()).thenReturn(List.of(card1));

        List<CardResponse> result = cardService.getCards(null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).title()).isEqualTo("Task 1");
    }

    @Test
    void getCards_filteredByColumn() {
        when(cardRepository.findByColumnIdOrderByPositionAsc(1L)).thenReturn(List.of(card1));

        List<CardResponse> result = cardService.getCards(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).columnId()).isEqualTo(1L);
    }

    @Test
    void createCard_assignsNextPosition() {
        when(columnRepository.findById(1L)).thenReturn(Optional.of(col1));
        when(cardRepository.findMaxPositionInColumn(1L)).thenReturn(2);
        when(cardRepository.save(any(Card.class))).thenAnswer(inv -> {
            Card c = inv.getArgument(0);
            c.setId(20L);
            c.setCreatedAt(LocalDateTime.now());
            c.setUpdatedAt(LocalDateTime.now());
            return c;
        });

        CardResponse result = cardService.createCard(
                new CardCreateRequest(1L, "New Card", null, "HIGH"));

        assertThat(result.position()).isEqualTo(3);
        assertThat(result.priority()).isEqualTo("HIGH");
    }

    @Test
    void createCard_noPriority() {
        when(columnRepository.findById(1L)).thenReturn(Optional.of(col1));
        when(cardRepository.findMaxPositionInColumn(1L)).thenReturn(-1);
        when(cardRepository.save(any(Card.class))).thenAnswer(inv -> {
            Card c = inv.getArgument(0);
            c.setId(21L);
            c.setCreatedAt(LocalDateTime.now());
            c.setUpdatedAt(LocalDateTime.now());
            return c;
        });

        CardResponse result = cardService.createCard(
                new CardCreateRequest(1L, "First Card", "desc", null));

        assertThat(result.position()).isEqualTo(0);
        assertThat(result.priority()).isNull();
    }

    @Test
    void createCard_throwsWhenColumnNotFound() {
        when(columnRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.createCard(
                new CardCreateRequest(99L, "Card", null, null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getCard_returnsCard() {
        when(cardRepository.findById(10L)).thenReturn(Optional.of(card1));

        CardResponse result = cardService.getCard(10L);

        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.title()).isEqualTo("Task 1");
    }

    @Test
    void getCard_throwsWhenNotFound() {
        when(cardRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.getCard(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void replaceCard_updatesAllFields() {
        when(cardRepository.findById(10L)).thenReturn(Optional.of(card1));
        when(columnRepository.findById(2L)).thenReturn(Optional.of(col2));
        when(cardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CardResponse result = cardService.replaceCard(10L,
                new CardUpdateRequest("Updated", "new desc", "HIGH", 2L, 5));

        assertThat(result.title()).isEqualTo("Updated");
        assertThat(result.description()).isEqualTo("new desc");
        assertThat(result.priority()).isEqualTo("HIGH");
        assertThat(result.position()).isEqualTo(5);
    }

    @Test
    void replaceCard_nullPriority() {
        when(cardRepository.findById(10L)).thenReturn(Optional.of(card1));
        when(cardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CardResponse result = cardService.replaceCard(10L,
                new CardUpdateRequest("Updated", null, null, null, null));

        assertThat(result.priority()).isNull();
    }

    @Test
    void replaceCard_throwsWhenTitleBlank() {
        assertThatThrownBy(() -> cardService.replaceCard(10L,
                new CardUpdateRequest("", null, null, null, null)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void replaceCard_throwsWhenTitleNull() {
        assertThatThrownBy(() -> cardService.replaceCard(10L,
                new CardUpdateRequest(null, null, null, null, null)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void replaceCard_throwsWhenNotFound() {
        when(cardRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.replaceCard(99L,
                new CardUpdateRequest("Title", null, null, null, null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void partialUpdateCard_updatesOnlyProvidedFields() {
        when(cardRepository.findById(10L)).thenReturn(Optional.of(card1));
        when(cardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CardResponse result = cardService.partialUpdateCard(10L,
                new CardUpdateRequest(null, "updated desc", null, null, null));

        assertThat(result.title()).isEqualTo("Task 1");
        assertThat(result.description()).isEqualTo("updated desc");
    }

    @Test
    void partialUpdateCard_updatesAllFields() {
        when(cardRepository.findById(10L)).thenReturn(Optional.of(card1));
        when(columnRepository.findById(2L)).thenReturn(Optional.of(col2));
        when(cardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CardResponse result = cardService.partialUpdateCard(10L,
                new CardUpdateRequest("New Title", "desc", "CRITICAL", 2L, 3));

        assertThat(result.title()).isEqualTo("New Title");
        assertThat(result.priority()).isEqualTo("CRITICAL");
        assertThat(result.position()).isEqualTo(3);
    }

    @Test
    void deleteCard_removesAndClosesGap() {
        when(cardRepository.findById(10L)).thenReturn(Optional.of(card1));

        cardService.deleteCard(10L);

        verify(cardRepository).delete(card1);
        verify(cardRepository).decrementPositionsAfterInColumn(1L, 0);
    }

    @Test
    void deleteCard_throwsWhenNotFound() {
        when(cardRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.deleteCard(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void moveCard_sameColumnForward() {
        card1.setPosition(1);
        when(cardRepository.findById(10L)).thenReturn(Optional.of(card1));
        when(cardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CardResponse result = cardService.moveCard(10L, new CardMoveRequest(1L, 3));

        verify(cardRepository).shiftDownBetween(1L, 2, 3);
        assertThat(result.position()).isEqualTo(3);
    }

    @Test
    void moveCard_sameColumnBackward() {
        card1.setPosition(3);
        when(cardRepository.findById(10L)).thenReturn(Optional.of(card1));
        when(cardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CardResponse result = cardService.moveCard(10L, new CardMoveRequest(1L, 1));

        verify(cardRepository).shiftUpBetween(1L, 1, 2);
        assertThat(result.position()).isEqualTo(1);
    }

    @Test
    void moveCard_sameColumnSamePosition() {
        card1.setPosition(2);
        when(cardRepository.findById(10L)).thenReturn(Optional.of(card1));

        CardResponse result = cardService.moveCard(10L, new CardMoveRequest(1L, 2));

        verify(cardRepository, never()).shiftDownBetween(any(), anyInt(), anyInt());
        verify(cardRepository, never()).shiftUpBetween(any(), anyInt(), anyInt());
        assertThat(result.position()).isEqualTo(2);
    }

    @Test
    void moveCard_crossColumn() {
        when(cardRepository.findById(10L)).thenReturn(Optional.of(card1));
        when(columnRepository.findById(2L)).thenReturn(Optional.of(col2));
        when(cardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CardResponse result = cardService.moveCard(10L, new CardMoveRequest(2L, 0));

        verify(cardRepository).decrementPositionsAfterInColumn(1L, 0);
        verify(cardRepository).incrementPositionsFromInColumn(2L, 0);
        assertThat(result.columnId()).isEqualTo(2L);
        assertThat(result.position()).isEqualTo(0);
    }

    @Test
    void moveCard_throwsWhenCardNotFound() {
        when(cardRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.moveCard(99L, new CardMoveRequest(1L, 0)))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
