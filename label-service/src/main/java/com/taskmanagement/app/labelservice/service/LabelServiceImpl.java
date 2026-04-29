package com.taskmanagement.app.labelservice.service;

import com.taskmanagement.app.labelservice.dto.*;
import com.taskmanagement.app.labelservice.entity.*;
import com.taskmanagement.app.labelservice.exception.BadRequestException;
import com.taskmanagement.app.labelservice.exception.ResourceNotFoundException;
import com.taskmanagement.app.labelservice.feign.BoardServiceClient;
import com.taskmanagement.app.labelservice.feign.CardServiceClient;
import com.taskmanagement.app.labelservice.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LabelServiceImpl implements LabelService {

    @Autowired
    private LabelRepository labelRepository;
    @Autowired
    private CardLabelRepository cardLabelRepository;
    @Autowired
    private ChecklistRepository checklistRepository;
    @Autowired
    private ChecklistItemRepository itemRepository;
    @Autowired
    private BoardServiceClient boardServiceClient;
    @Autowired
    private CardServiceClient cardServiceClient;



    @Override
    @Transactional
    public LabelResponse createLabel(CreateLabelRequest req, String token) {
        validateBoard(req.getBoardId(), token);
        if (labelRepository.existsByBoardIdAndName(req.getBoardId(), req.getName()))
            throw new BadRequestException("Label '" + req.getName() + "' already exists on this board");
        Label label = new Label();
        label.setBoardId(req.getBoardId());
        label.setName(req.getName());
        label.setColor(req.getColor());
        return toLabelResponse(labelRepository.save(label));
    }

    @Override
    public List<LabelResponse> getLabelsByBoard(Long boardId) {
        return labelRepository.findByBoardId(boardId).stream()
                .map(this::toLabelResponse).collect(Collectors.toList());
    }

    @Override
    public LabelResponse getLabelById(Long labelId) {
        return toLabelResponse(findLabelOrThrow(labelId));
    }

    @Override
    @Transactional
    public LabelResponse updateLabel(Long labelId, UpdateLabelRequest req) {
        Label label = findLabelOrThrow(labelId);
        if (req.getName() != null && !req.getName().isBlank())
            label.setName(req.getName());
        if (req.getColor() != null)
            label.setColor(req.getColor());
        return toLabelResponse(labelRepository.save(label));
    }

    @Override
    @Transactional
    public void deleteLabel(Long labelId) {
        Label label = findLabelOrThrow(labelId);
        cardLabelRepository.deleteByLabelId(labelId); // Bulk delete (fixed N+1)
        labelRepository.delete(label);
    }

    @Override
    @Transactional
    public void addLabelToCard(CardLabelRequest req, String token) {
        findLabelOrThrow(req.getLabelId()); // Validate label exists
        validateCard(req.getCardId(), token); // Validate card exists
        if (cardLabelRepository.existsByCardIdAndLabelId(req.getCardId(), req.getLabelId()))
            throw new BadRequestException("Label already attached to this card");
        CardLabel cl = new CardLabel();
        cl.setCardId(req.getCardId());
        cl.setLabelId(req.getLabelId());
        cardLabelRepository.save(cl);
    }

    @Override
    @Transactional
    public void removeLabelFromCard(Long cardId, Long labelId) {
        if (!cardLabelRepository.existsByCardIdAndLabelId(cardId, labelId))
            throw new BadRequestException("Label is not attached to this card");
        cardLabelRepository.deleteByCardIdAndLabelId(cardId, labelId);
    }

    @Override
    public List<LabelResponse> getLabelsForCard(Long cardId) {
        return cardLabelRepository.findByCardId(cardId).stream()
                .map(cl -> toLabelResponse(findLabelOrThrow(cl.getLabelId())))
                .collect(Collectors.toList());
    }

    // ── Checklists ────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ChecklistResponse createChecklist(CreateChecklistRequest req, String token) {
        validateCard(req.getCardId(), token); // Validate card exists
        Checklist checklist = new Checklist();
        checklist.setCardId(req.getCardId());
        checklist.setTitle(req.getTitle());
        checklist.setPosition(req.getPosition() != null ? req.getPosition() : 0);
        return toChecklistResponse(checklistRepository.save(checklist));
    }

    @Override
    public ChecklistResponse getChecklistById(Long checklistId) {
        return toChecklistResponse(findChecklistOrThrow(checklistId));
    }

    @Override
    public List<ChecklistResponse> getChecklistsByCard(Long cardId) {
        return checklistRepository.findByCardIdOrderByPosition(cardId)
                .stream().map(this::toChecklistResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteChecklist(Long checklistId) {
        checklistRepository.delete(findChecklistOrThrow(checklistId));
    }

    @Override
    @Transactional
    public ChecklistItemResponse addItem(Long checklistId, AddChecklistItemRequest req) {
        Checklist checklist = findChecklistOrThrow(checklistId);
        ChecklistItem item = new ChecklistItem();
        item.setChecklist(checklist);
        item.setText(req.getText());
        item.setAssigneeId(req.getAssigneeId());
        item.setDueDate(req.getDueDate());
        return toItemResponse(itemRepository.save(item));
    }

    @Override
    @Transactional
    public ChecklistItemResponse toggleItem(Long itemId) {
        ChecklistItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Checklist item not found: " + itemId));
        item.setCompleted(!item.isCompleted());
        return toItemResponse(itemRepository.save(item));
    }

    @Override
    @Transactional
    public void deleteItem(Long itemId) {
        ChecklistItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Checklist item not found: " + itemId));
        itemRepository.delete(item);
    }

    @Override
    public int getChecklistProgress(Long checklistId) {
        long total = itemRepository.countByChecklist_ChecklistId(checklistId);
        if (total == 0)
            return 0;
        long completed = itemRepository.countByChecklist_ChecklistIdAndIsCompletedTrue(checklistId);
        return (int) ((completed * 100) / total);
    }

    // ── Private Helpers ───────────────────────────────────────────────────

    private Label findLabelOrThrow(Long id) {
        return labelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Label not found: " + id));
    }

    private Checklist findChecklistOrThrow(Long id) {
        return checklistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Checklist not found: " + id));
    }

    private LabelResponse toLabelResponse(Label l) {
        LabelResponse r = new LabelResponse();
        r.setLabelId(l.getLabelId());
        r.setBoardId(l.getBoardId());
        r.setName(l.getName());
        r.setColor(l.getColor());
        r.setCreatedAt(l.getCreatedAt());
        return r;
    }

    private ChecklistResponse toChecklistResponse(Checklist c) {
        List<ChecklistItem> items = itemRepository.findByChecklist_ChecklistId(c.getChecklistId());
        long total = items.size();
        long completed = items.stream().filter(ChecklistItem::isCompleted).count();
        ChecklistResponse r = new ChecklistResponse();
        r.setChecklistId(c.getChecklistId());
        r.setCardId(c.getCardId());
        r.setTitle(c.getTitle());
        r.setPosition(c.getPosition());
        r.setCreatedAt(c.getCreatedAt());
        r.setTotalCount((int) total);
        r.setCompletedCount((int) completed);
        r.setProgressPercent(total == 0 ? 0 : (int) ((completed * 100) / total));
        r.setItems(items.stream().map(this::toItemResponse).collect(Collectors.toList()));
        return r;
    }

    private ChecklistItemResponse toItemResponse(ChecklistItem i) {
        ChecklistItemResponse r = new ChecklistItemResponse();
        r.setItemId(i.getItemId());
        r.setChecklistId(i.getChecklist().getChecklistId());
        r.setText(i.getText());
        r.setCompleted(i.isCompleted());
        r.setAssigneeId(i.getAssigneeId());
        r.setDueDate(i.getDueDate());
        return r;
    }

    private void validateBoard(Long boardId, String token) {
        try {
            ResponseEntity<BoardResponse> res = boardServiceClient.getBoardById(boardId, token);
            if (res.getBody() == null)
                throw new ResourceNotFoundException("Board not found: " + boardId);
        } catch (Exception e) {
            throw new ResourceNotFoundException("Board not found: " + boardId);
        }
    }

    private void validateCard(Long cardId, String token) {
        try {
            ResponseEntity<CardResponse> res = cardServiceClient.getCardById(cardId, token);
            if (res.getBody() == null)
                throw new ResourceNotFoundException("Card not found: " + cardId);
        } catch (Exception e) {
            throw new ResourceNotFoundException("Card not found: " + cardId);
        }
    }
}
