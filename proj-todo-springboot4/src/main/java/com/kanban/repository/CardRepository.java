package com.kanban.repository;

import com.kanban.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CardRepository extends JpaRepository<Card, Long> {

    List<Card> findByColumnIdOrderByPositionAsc(Long columnId);

    @Query("SELECT c FROM Card c ORDER BY c.column.position ASC, c.position ASC")
    List<Card> findAllOrderedByColumnAndPosition();

    @Query("SELECT COALESCE(MAX(c.position), -1) FROM Card c WHERE c.column.id = :columnId")
    Integer findMaxPositionInColumn(@Param("columnId") Long columnId);

    long countByColumnId(Long columnId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Card c SET c.position = c.position - 1 WHERE c.column.id = :columnId AND c.position > :position")
    void decrementPositionsAfterInColumn(@Param("columnId") Long columnId, @Param("position") int position);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Card c SET c.position = c.position + 1 WHERE c.column.id = :columnId AND c.position >= :position")
    void incrementPositionsFromInColumn(@Param("columnId") Long columnId, @Param("position") int position);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Card c SET c.position = c.position - 1 WHERE c.column.id = :columnId AND c.position BETWEEN :from AND :to")
    void shiftDownBetween(@Param("columnId") Long columnId, @Param("from") int from, @Param("to") int to);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Card c SET c.position = c.position + 1 WHERE c.column.id = :columnId AND c.position BETWEEN :from AND :to")
    void shiftUpBetween(@Param("columnId") Long columnId, @Param("from") int from, @Param("to") int to);
}
