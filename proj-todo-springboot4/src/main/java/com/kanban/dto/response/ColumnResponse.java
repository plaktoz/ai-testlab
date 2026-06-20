package com.kanban.dto.response;

public record ColumnResponse(
        Long id,
        String name,
        String color,
        Integer position
) {}
