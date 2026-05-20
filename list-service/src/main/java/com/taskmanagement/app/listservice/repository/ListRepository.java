package com.taskmanagement.app.listservice.repository;

import com.taskmanagement.app.listservice.entity.TaskList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ListRepository extends JpaRepository<TaskList, Long> {

    List<TaskList> findByBoardIdOrderByPosition(Long boardId);

    List<TaskList> findByBoardIdAndIsArchived(Long boardId, boolean isArchived);

    long countByBoardId(Long boardId);

    @Query("SELECT MAX(t.position) FROM TaskList t WHERE t.boardId = :boardId")
    Optional<Integer> findMaxPositionByBoardId(@Param("boardId") Long boardId);

    void deleteByListId(Long listId);
}
