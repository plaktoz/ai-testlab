package com.kanban.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CardMoveRequest(
        @NotNull Long columnId,
        @NotNull @Min(0) Integer position
) {}
