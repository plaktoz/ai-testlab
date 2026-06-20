package com.kanban.repository;

import com.kanban.entity.BoardColumn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ColumnRepository extends JpaRepository<BoardColumn, Long> {

    List<BoardColumn> findAllByOrderByPositionAsc();

    @Query("SELECT COALESCE(MAX(c.position), -1) FROM BoardColumn c")
    Integer findMaxPosition();
}
