package com.kanban.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ColumnReorderRequest(
        @NotNull List<Long> columnIds
) {}
