package com.taskmanagement.app.labelservice.repository;

import com.taskmanagement.app.labelservice.entity.CardLabel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CardLabelRepository extends JpaRepository<CardLabel, Long> {
    List<CardLabel> findByCardId(Long cardId);

    List<CardLabel> findByLabelId(Long labelId);

    boolean existsByCardIdAndLabelId(Long cardId, Long labelId);

    void deleteByCardIdAndLabelId(Long cardId, Long labelId);

    void deleteByLabelId(Long labelId);
}
