package com.taskmanagement.app.labelservice.entity;
import jakarta.persistence.*;  import lombok.Data; import lombok.NoArgsConstructor;
@Entity @Data @NoArgsConstructor
@Table(name = "card_labels", uniqueConstraints = @UniqueConstraint(columnNames = {"card_id","label_id"}))
public class CardLabel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long cardId;

    @Column(nullable = false)
    private Long labelId;
}
