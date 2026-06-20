package com.kanban.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ColumnCreateRequest(
        @NotBlank @Size(max = 255) String name,
        @Size(max = 7) String color
) {}
