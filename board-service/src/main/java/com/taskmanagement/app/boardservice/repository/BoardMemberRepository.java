package com.taskmanagement.app.boardservice.repository;

import com.taskmanagement.app.boardservice.entity.BoardMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoardMemberRepository extends JpaRepository<BoardMember, Long> {

    List<BoardMember> findByBoard_BoardId(Long boardId);

    Optional<BoardMember> findByBoard_BoardIdAndUserId(Long boardId, Long userId);

    boolean existsByBoard_BoardIdAndUserId(Long boardId, Long userId);

    void deleteByBoard_BoardIdAndUserId(Long boardId, Long userId);
}
