package com.taskmanagement.app.commentservice.repository;

import com.taskmanagement.app.commentservice.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByCardIdAndIsDeletedFalseAndParentCommentIdIsNullOrderByCreatedAtAsc(Long cardId);

    List<Comment> findByParentCommentIdAndIsDeletedFalseOrderByCreatedAtAsc(Long parentCommentId);

    List<Comment> findByAuthorId(Long authorId);

    long countByCardIdAndIsDeletedFalse(Long cardId);
}
