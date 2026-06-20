package com.kanban.dto.request;

import jakarta.validation.constraints.Size;

public record ColumnUpdateRequest(
        @Size(min = 1, max = 255) String name,
        @Size(max = 7) String color
) {}
