package com.taskmanagement.app.boardservice.repository;

import com.taskmanagement.app.boardservice.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {

    List<Board> findByWorkspaceId(Long workspaceId);

    List<Board> findByCreatedById(Long userId);

    @Query("SELECT b FROM Board b JOIN b.members m WHERE m.userId = :userId")
    List<Board> findByMemberUserId(@Param("userId") Long userId);

    List<Board> findByVisibility(String visibility);

    long countByWorkspaceId(Long workspaceId);

    List<Board> findByIsClosed(boolean isClosed);
}
