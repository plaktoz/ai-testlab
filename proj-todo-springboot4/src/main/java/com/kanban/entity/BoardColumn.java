package com.kanban.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "board_columns")
public class BoardColumn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 7)
    private String color;

    @Column(nullable = false)
    private Integer position;

    public BoardColumn() {}

    public BoardColumn(String name, String color, Integer position) {
        this.name = name;
        this.color = color;
        this.position = position;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public Integer getPosition() { return position; }
    public void setPosition(Integer position) { this.position = position; }
}
