package com.taskmanagement.app.labelservice.entity;
import jakarta.persistence.*; import lombok.Data; import lombok.NoArgsConstructor;
import java.time.LocalDateTime; import java.util.ArrayList; import java.util.List;
@Entity @Data @NoArgsConstructor @Table(name = "checklists")
public class Checklist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long checklistId;

    @Column(nullable = false)
    private Long cardId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private Integer position;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "checklist", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ChecklistItem> items = new ArrayList<>();

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
