package com.kanban.entity;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class BoardColumnTest {

    @Test
    void constructor_setsFields() {
        BoardColumn column = new BoardColumn("Todo", "#FF0000", 0);

        assertThat(column.getName()).isEqualTo("Todo");
        assertThat(column.getColor()).isEqualTo("#FF0000");
        assertThat(column.getPosition()).isEqualTo(0);
    }

    @Test
    void setters_updateFields() {
        BoardColumn column = new BoardColumn();
        column.setId(1L);
        column.setName("Done");
        column.setColor("#00FF00");
        column.setPosition(1);

        assertThat(column.getId()).isEqualTo(1L);
        assertThat(column.getName()).isEqualTo("Done");
        assertThat(column.getColor()).isEqualTo("#00FF00");
        assertThat(column.getPosition()).isEqualTo(1);
    }
}
