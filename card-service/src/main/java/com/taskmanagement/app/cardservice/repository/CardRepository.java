package com.taskmanagement.app.cardservice.repository;

import com.taskmanagement.app.cardservice.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    List<Card> findByListIdOrderByPosition(Long listId);
    List<Card> findByBoardId(Long boardId);
    List<Card> findByAssigneeId(Long assigneeId);
    Optional<Card> findByCardId(Long cardId);
    List<Card> findByBoardIdAndIsArchived(Long boardId, boolean isArchived);
    List<Card> findByListIdAndIsArchived(Long listId, boolean isArchived);
    /** Cards where dueDate is before today and status is not DONE */
    @Query("SELECT c FROM Card c WHERE c.dueDate < :today AND c.status <> 'DONE' AND c.isArchived = false")
    List<Card> findOverdueCards(@Param("today") LocalDate today);
    /** Overdue cards scoped to a specific board */
    @Query("SELECT c FROM Card c WHERE c.boardId = :boardId AND c.dueDate < :today AND c.status <> 'DONE' AND c.isArchived = false")
    List<Card> findOverdueCardsByBoard(@Param("boardId") Long boardId, @Param("today") LocalDate today);
    List<Card> findByPriority(String priority);
    List<Card> findByStatus(String status);
    long countByListId(Long listId);
    @Query("SELECT MAX(c.position) FROM Card c WHERE c.listId = :listId")
    Optional<Integer> findMaxPositionByListId(@Param("listId") Long listId);
}
