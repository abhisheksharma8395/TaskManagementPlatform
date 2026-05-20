package com.taskmanagement.app.labelservice.entity;
import jakarta.persistence.*; import lombok.Data; import lombok.NoArgsConstructor;
import java.time.LocalDate;
@Entity @Data @NoArgsConstructor @Table(name = "checklist_items")
public class ChecklistItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long itemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checklist_id", nullable = false)
    private Checklist checklist;

    @Column(nullable = false)
    private String text;

    @Column(nullable = false)
    private boolean isCompleted = false;

    private Long assigneeId;
    private LocalDate dueDate;
}
