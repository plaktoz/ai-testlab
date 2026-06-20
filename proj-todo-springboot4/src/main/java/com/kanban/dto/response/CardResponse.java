package com.kanban.dto.response;

import java.time.LocalDateTime;

public record CardResponse(
        Long id,
        Long columnId,
        String title,
        String description,
        String priority,
        Integer position,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
