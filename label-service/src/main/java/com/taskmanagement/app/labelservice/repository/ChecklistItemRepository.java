package com.taskmanagement.app.labelservice.repository;

import com.taskmanagement.app.labelservice.entity.ChecklistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChecklistItemRepository extends JpaRepository<ChecklistItem, Long> {
    List<ChecklistItem> findByChecklist_ChecklistId(Long checklistId);

    long countByChecklist_ChecklistId(Long checklistId);

    long countByChecklist_ChecklistIdAndIsCompletedTrue(Long checklistId);
}
