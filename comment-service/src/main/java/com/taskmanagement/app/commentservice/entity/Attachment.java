package com.taskmanagement.app.commentservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "attachments")
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long attachmentId;

    @Column(nullable = false)
    private Long cardId;

    @Column(nullable = false)
    private Long uploaderId;

    @Column(nullable = false)
    private String fileName;

    /** S3 / CDN URL of the uploaded file */
    @Column(nullable = false)
    private String fileKey;


    private String fileType;

    /** File size in Kb */
    private Long sizeKb;

    @Column(nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    @PrePersist
    void onCreate() { uploadedAt = LocalDateTime.now(); }
}
