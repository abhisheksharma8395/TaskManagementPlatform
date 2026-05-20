package com.taskmanagement.app.labelservice.repository;

import com.taskmanagement.app.labelservice.entity.Label;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LabelRepository extends JpaRepository<Label, Long> {
    List<Label> findByBoardId(Long boardId);

    boolean existsByBoardIdAndName(Long boardId, String name);
}
