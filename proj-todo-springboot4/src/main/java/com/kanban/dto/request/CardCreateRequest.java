package com.kanban.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CardCreateRequest(
        @NotNull Long columnId,
        @NotBlank @Size(max = 255) String title,
        String description,
        String priority
) {}
