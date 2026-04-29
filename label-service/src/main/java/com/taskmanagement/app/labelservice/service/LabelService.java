package com.taskmanagement.app.labelservice.service;

import com.taskmanagement.app.labelservice.dto.*;
import java.util.List;

public interface LabelService {

    // Labels
    LabelResponse createLabel(CreateLabelRequest request, String token);

    List<LabelResponse> getLabelsByBoard(Long boardId);

    LabelResponse getLabelById(Long labelId);

    LabelResponse updateLabel(Long labelId, UpdateLabelRequest request);

    void deleteLabel(Long labelId);

    void addLabelToCard(CardLabelRequest request, String token);

    void removeLabelFromCard(Long cardId, Long labelId);

    List<LabelResponse> getLabelsForCard(Long cardId);

    // Checklists
    ChecklistResponse createChecklist(CreateChecklistRequest request, String token);

    ChecklistResponse getChecklistById(Long checklistId);

    List<ChecklistResponse> getChecklistsByCard(Long cardId);

    void deleteChecklist(Long checklistId);

    ChecklistItemResponse addItem(Long checklistId, AddChecklistItemRequest request);

    ChecklistItemResponse toggleItem(Long itemId);

    void deleteItem(Long itemId);

    int getChecklistProgress(Long checklistId);
}
