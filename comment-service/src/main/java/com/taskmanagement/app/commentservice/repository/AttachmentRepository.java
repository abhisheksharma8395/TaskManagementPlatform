package com.taskmanagement.app.commentservice.repository;

import com.taskmanagement.app.commentservice.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    List<Attachment> findByCardId(Long cardId);

    List<Attachment> findByUploaderId(Long uploaderId);
}
