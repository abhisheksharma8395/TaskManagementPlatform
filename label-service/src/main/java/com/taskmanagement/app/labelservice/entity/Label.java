package com.taskmanagement.app.labelservice.entity;
import jakarta.persistence.*; import lombok.Data; import lombok.NoArgsConstructor;
import java.time.LocalDate;
@Entity @Data @NoArgsConstructor @Table(name = "labels")
public class Label {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long labelId;

    @Column(nullable = false)
    private Long boardId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String color;

    @Column(nullable = false, updatable = false)
    private LocalDate createdAt;

    @PrePersist void onCreate() {
        createdAt = LocalDate.now();
    }
}
