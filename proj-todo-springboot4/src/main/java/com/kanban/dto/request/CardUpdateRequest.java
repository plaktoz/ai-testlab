package com.kanban.dto.request;

import jakarta.validation.constraints.Size;

public record CardUpdateRequest(
        @Size(min = 1, max = 255) String title,
        String description,
        String priority,
        Long columnId,
        Integer position
) {}
