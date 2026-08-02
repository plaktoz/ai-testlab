package com.kanban.entity;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class CardTest {

    @Test
    void setters_updateFields() {
        Card card = new Card();
        BoardColumn column = new BoardColumn("Todo", "#FF0000", 0);
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2024, 1, 2, 0, 0);

        card.setId(10L);
        card.setColumn(column);
        card.setTitle("Task");
        card.setDescription("Description");
        card.setPriority(Card.Priority.HIGH);
        card.setPosition(2);
        card.setCreatedAt(createdAt);
        card.setUpdatedAt(updatedAt);

        assertThat(card.getId()).isEqualTo(10L);
        assertThat(card.getColumn()).isEqualTo(column);
        assertThat(card.getTitle()).isEqualTo("Task");
        assertThat(card.getDescription()).isEqualTo("Description");
        assertThat(card.getPriority()).isEqualTo(Card.Priority.HIGH);
        assertThat(card.getPosition()).isEqualTo(2);
        assertThat(card.getCreatedAt()).isEqualTo(createdAt);
        assertThat(card.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    void lifecycleCallbacks_setTimestampFields() {
        Card card = new Card();
        card.onCreate();

        assertThat(card.getCreatedAt()).isNotNull();
        assertThat(card.getUpdatedAt()).isNotNull();

        LocalDateTime previousUpdatedAt = card.getUpdatedAt();
        card.onUpdate();

        assertThat(card.getUpdatedAt()).isAfterOrEqualTo(previousUpdatedAt);
    }
}
