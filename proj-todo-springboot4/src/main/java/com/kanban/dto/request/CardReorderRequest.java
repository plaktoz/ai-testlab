package com.kanban.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CardReorderRequest(
        @NotNull List<Long> cardIds
) {}
