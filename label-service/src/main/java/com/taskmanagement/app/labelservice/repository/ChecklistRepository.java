package com.taskmanagement.app.labelservice.repository;

import com.taskmanagement.app.labelservice.entity.Checklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChecklistRepository extends JpaRepository<Checklist, Long> {
    List<Checklist> findByCardIdOrderByPosition(Long cardId);
}
