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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ColumnServiceTest {

    @Mock
    private ColumnRepository columnRepository;

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private ColumnService columnService;

    private BoardColumn col1;
    private BoardColumn col2;

    @BeforeEach
    void setUp() {
        col1 = new BoardColumn();
        col1.setId(1L);
        col1.setName("Todo");
        col1.setColor("#FF0000");
        col1.setPosition(0);

        col2 = new BoardColumn();
        col2.setId(2L);
        col2.setName("Done");
        col2.setColor("#00FF00");
        col2.setPosition(1);
    }

    @Test
    void getColumns_returnsAllColumnsSorted() {
        when(columnRepository.findAllByOrderByPositionAsc()).thenReturn(List.of(col1, col2));

        List<ColumnResponse> result = columnService.getColumns();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).name()).isEqualTo("Todo");
        assertThat(result.get(1).name()).isEqualTo("Done");
    }

    @Test
    void createColumn_assignsNextPosition() {
        when(columnRepository.findMaxPosition()).thenReturn(1);
        BoardColumn saved = new BoardColumn();
        saved.setId(3L);
        saved.setName("In Progress");
        saved.setColor(null);
        saved.setPosition(2);
        when(columnRepository.save(any(BoardColumn.class))).thenReturn(saved);

        ColumnResponse result = columnService.createColumn(new ColumnCreateRequest("In Progress", null));

        assertThat(result.position()).isEqualTo(2);
        assertThat(result.name()).isEqualTo("In Progress");
    }

    @Test
    void createColumn_firstColumnGetsPositionZero() {
        when(columnRepository.findMaxPosition()).thenReturn(-1);
        BoardColumn saved = new BoardColumn();
        saved.setId(1L);
        saved.setName("Backlog");
        saved.setColor(null);
        saved.setPosition(0);
        when(columnRepository.save(any(BoardColumn.class))).thenReturn(saved);

        ColumnResponse result = columnService.createColumn(new ColumnCreateRequest("Backlog", null));

        assertThat(result.position()).isEqualTo(0);
    }

    @Test
    void getColumn_returnsColumn() {
        when(columnRepository.findById(1L)).thenReturn(Optional.of(col1));

        ColumnResponse result = columnService.getColumn(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Todo");
    }

    @Test
    void getColumn_throwsWhenNotFound() {
        when(columnRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> columnService.getColumn(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void replaceColumn_updatesAllFields() {
        when(columnRepository.findById(1L)).thenReturn(Optional.of(col1));
        when(columnRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ColumnResponse result = columnService.replaceColumn(1L, new ColumnUpdateRequest("Updated", "#0000FF"));

        assertThat(result.name()).isEqualTo("Updated");
        assertThat(result.color()).isEqualTo("#0000FF");
    }

    @Test
    void replaceColumn_throwsWhenNameBlank() {
        assertThatThrownBy(() -> columnService.replaceColumn(1L, new ColumnUpdateRequest("", "#FFF")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void replaceColumn_throwsWhenNameNull() {
        assertThatThrownBy(() -> columnService.replaceColumn(1L, new ColumnUpdateRequest(null, null)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void replaceColumn_throwsWhenColumnNotFound() {
        when(columnRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> columnService.replaceColumn(99L, new ColumnUpdateRequest("Name", null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void partialUpdateColumn_updatesOnlyProvidedFields() {
        when(columnRepository.findById(1L)).thenReturn(Optional.of(col1));
        when(columnRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ColumnResponse result = columnService.partialUpdateColumn(1L, new ColumnUpdateRequest(null, "#AABBCC"));

        assertThat(result.name()).isEqualTo("Todo");
        assertThat(result.color()).isEqualTo("#AABBCC");
    }

    @Test
    void partialUpdateColumn_updatesName() {
        when(columnRepository.findById(1L)).thenReturn(Optional.of(col1));
        when(columnRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ColumnResponse result = columnService.partialUpdateColumn(1L, new ColumnUpdateRequest("New Name", null));

        assertThat(result.name()).isEqualTo("New Name");
        assertThat(result.color()).isEqualTo("#FF0000");
    }

    @Test
    void deleteColumn_deletesWhenNoCards() {
        when(columnRepository.findById(1L)).thenReturn(Optional.of(col1));
        when(cardRepository.countByColumnId(1L)).thenReturn(0L);

        columnService.deleteColumn(1L);

        verify(columnRepository).delete(col1);
    }

    @Test
    void deleteColumn_throwsConflictWhenCardsExist() {
        when(columnRepository.findById(1L)).thenReturn(Optional.of(col1));
        when(cardRepository.countByColumnId(1L)).thenReturn(3L);

        assertThatThrownBy(() -> columnService.deleteColumn(1L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("cards");
    }

    @Test
    void deleteColumn_throwsWhenColumnNotFound() {
        when(columnRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> columnService.deleteColumn(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void reorderColumns_setsPositionsByOrder() {
        when(columnRepository.findAllByOrderByPositionAsc()).thenReturn(List.of(col1, col2));
        when(columnRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        List<ColumnResponse> result = columnService.reorderColumns(new ColumnReorderRequest(List.of(2L, 1L)));

        assertThat(result.get(0).id()).isEqualTo(2L);
        assertThat(result.get(0).position()).isEqualTo(0);
        assertThat(result.get(1).id()).isEqualTo(1L);
        assertThat(result.get(1).position()).isEqualTo(1);
    }

    @Test
    void reorderColumns_throwsWhenMissingIds() {
        when(columnRepository.findAllByOrderByPositionAsc()).thenReturn(List.of(col1, col2));

        assertThatThrownBy(() -> columnService.reorderColumns(new ColumnReorderRequest(List.of(1L))))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void reorderColumns_throwsWhenExtraIds() {
        when(columnRepository.findAllByOrderByPositionAsc()).thenReturn(List.of(col1, col2));

        assertThatThrownBy(() -> columnService.reorderColumns(new ColumnReorderRequest(List.of(1L, 2L, 3L))))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void reorderColumns_throwsWhenWrongIds() {
        when(columnRepository.findAllByOrderByPositionAsc()).thenReturn(List.of(col1, col2));

        assertThatThrownBy(() -> columnService.reorderColumns(new ColumnReorderRequest(List.of(1L, 3L))))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void reorderCardsInColumn_setsPositions() {
        when(columnRepository.findById(1L)).thenReturn(Optional.of(col1));

        Card c1 = makeCard(10L, col1, "Card A", 0);
        Card c2 = makeCard(11L, col1, "Card B", 1);
        when(cardRepository.findByColumnIdOrderByPositionAsc(1L)).thenReturn(List.of(c1, c2));
        when(cardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        List<Card> result = columnService.reorderCardsInColumn(1L, new CardReorderRequest(List.of(11L, 10L)));

        assertThat(result.get(0).getId()).isEqualTo(11L);
        assertThat(result.get(0).getPosition()).isEqualTo(0);
        assertThat(result.get(1).getId()).isEqualTo(10L);
        assertThat(result.get(1).getPosition()).isEqualTo(1);
    }

    @Test
    void reorderCardsInColumn_throwsWhenColumnNotFound() {
        when(columnRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> columnService.reorderCardsInColumn(99L, new CardReorderRequest(List.of(1L))))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void reorderCardsInColumn_throwsWhenMissingCardIds() {
        when(columnRepository.findById(1L)).thenReturn(Optional.of(col1));
        Card c1 = makeCard(10L, col1, "Card A", 0);
        Card c2 = makeCard(11L, col1, "Card B", 1);
        when(cardRepository.findByColumnIdOrderByPositionAsc(1L)).thenReturn(List.of(c1, c2));

        assertThatThrownBy(() -> columnService.reorderCardsInColumn(1L, new CardReorderRequest(List.of(10L))))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private Card makeCard(Long id, BoardColumn column, String title, int position) {
        Card card = new Card();
        card.setId(id);
        card.setColumn(column);
        card.setTitle(title);
        card.setPosition(position);
        return card;
    }
}
