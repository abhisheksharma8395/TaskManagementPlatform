package com.taskmanagement.app.labelservice.service;

import com.taskmanagement.app.labelservice.dto.*;
import com.taskmanagement.app.labelservice.entity.*;
import com.taskmanagement.app.labelservice.exception.BadRequestException;
import com.taskmanagement.app.labelservice.exception.ResourceNotFoundException;
import com.taskmanagement.app.labelservice.feign.BoardServiceClient;
import com.taskmanagement.app.labelservice.feign.CardServiceClient;
import com.taskmanagement.app.labelservice.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LabelServiceImplTest {

    @Mock
    private LabelRepository labelRepository;
    @Mock
    private CardLabelRepository cardLabelRepository;
    @Mock
    private ChecklistRepository checklistRepository;
    @Mock
    private ChecklistItemRepository itemRepository;
    @Mock
    private BoardServiceClient boardServiceClient;
    @Mock
    private CardServiceClient cardServiceClient;

    @InjectMocks
    private LabelServiceImpl labelService;

    private Label sampleLabel;
    private Checklist sampleChecklist;
    private ChecklistItem sampleItem;
    private final Long BOARD_ID = 5L;
    private final Long CARD_ID = 3L;
    private final Long LABEL_ID = 1L;
    private final Long CHECKLIST_ID = 10L;
    private final Long ITEM_ID = 100L;
    private final String TOKEN = "Bearer test.token";

    @BeforeEach
    void setUp() {
        sampleLabel = new Label();
        sampleLabel.setLabelId(LABEL_ID);
        sampleLabel.setBoardId(BOARD_ID);
        sampleLabel.setName("Bug");
        sampleLabel.setColor("#FF0000");

        sampleChecklist = new Checklist();
        sampleChecklist.setChecklistId(CHECKLIST_ID);
        sampleChecklist.setCardId(CARD_ID);
        sampleChecklist.setTitle("Test Checklist");
        sampleChecklist.setPosition(0);

        sampleItem = new ChecklistItem();
        sampleItem.setItemId(ITEM_ID);
        sampleItem.setChecklist(sampleChecklist);
        sampleItem.setText("Do something");
        sampleItem.setCompleted(false);
    }

    // ─── createLabel ──────────────────────────────────────────────────────────

    @Test
    void createLabel_happyPath_returnsResponse() {
        CreateLabelRequest req = new CreateLabelRequest();
        req.setBoardId(BOARD_ID);
        req.setName("Bug");
        req.setColor("#FF0000");

        when(boardServiceClient.getById(BOARD_ID, TOKEN)).thenReturn(ResponseEntity.ok(new BoardResponse()));
        when(labelRepository.existsByBoardIdAndName(BOARD_ID, "Bug")).thenReturn(false);
        when(labelRepository.save(any(Label.class))).thenReturn(sampleLabel);

        LabelResponse result = labelService.createLabel(req, TOKEN);

        assertThat(result).isNotNull();
        assertThat(result.getLabelId()).isEqualTo(LABEL_ID);
        verify(labelRepository).save(any(Label.class));
    }

    @Test
    void createLabel_boardNotFound_throwsResourceNotFound() {
        CreateLabelRequest req = new CreateLabelRequest();
        req.setBoardId(BOARD_ID);
        req.setName("Bug");

        when(boardServiceClient.getById(BOARD_ID, TOKEN)).thenThrow(new RuntimeException("Board not found"));

        assertThatThrownBy(() -> labelService.createLabel(req, TOKEN))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createLabel_duplicateName_throwsBadRequest() {
        CreateLabelRequest req = new CreateLabelRequest();
        req.setBoardId(BOARD_ID);
        req.setName("Bug");

        when(boardServiceClient.getById(BOARD_ID, TOKEN)).thenReturn(ResponseEntity.ok(new BoardResponse()));
        when(labelRepository.existsByBoardIdAndName(BOARD_ID, "Bug")).thenReturn(true);

        assertThatThrownBy(() -> labelService.createLabel(req, TOKEN))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already exists");
    }

    // ─── getLabelsByBoard ─────────────────────────────────────────────────────

    @Test
    void getLabelsByBoard_returnsList() {
        when(labelRepository.findByBoardId(BOARD_ID)).thenReturn(List.of(sampleLabel));

        List<LabelResponse> results = labelService.getLabelsByBoard(BOARD_ID);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Bug");
    }

    // ─── getLabelById ─────────────────────────────────────────────────────────

    @Test
    void getLabelById_found_returnsResponse() {
        when(labelRepository.findById(LABEL_ID)).thenReturn(Optional.of(sampleLabel));

        LabelResponse result = labelService.getLabelById(LABEL_ID);

        assertThat(result.getLabelId()).isEqualTo(LABEL_ID);
    }

    @Test
    void getLabelById_notFound_throwsResourceNotFound() {
        when(labelRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> labelService.getLabelById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── updateLabel ──────────────────────────────────────────────────────────

    @Test
    void updateLabel_succeeds() {
        UpdateLabelRequest req = new UpdateLabelRequest();
        req.setName("Feature");
        req.setColor("#00FF00");

        when(labelRepository.findById(LABEL_ID)).thenReturn(Optional.of(sampleLabel));
        when(labelRepository.save(any(Label.class))).thenReturn(sampleLabel);

        LabelResponse result = labelService.updateLabel(LABEL_ID, req);

        assertThat(result).isNotNull();
        verify(labelRepository).save(sampleLabel);
    }

    // ─── deleteLabel ──────────────────────────────────────────────────────────

    @Test
    void deleteLabel_found_succeeds() {
        when(labelRepository.findById(LABEL_ID)).thenReturn(Optional.of(sampleLabel));

        labelService.deleteLabel(LABEL_ID);

        verify(cardLabelRepository).deleteByLabelId(LABEL_ID);
        verify(labelRepository).delete(sampleLabel);
    }

    // ─── addLabelToCard ───────────────────────────────────────────────────────

    @Test
    void addLabelToCard_happyPath_succeeds() {
        CardLabelRequest req = new CardLabelRequest();
        req.setLabelId(LABEL_ID);
        req.setCardId(CARD_ID);

        when(labelRepository.findById(LABEL_ID)).thenReturn(Optional.of(sampleLabel));
        when(cardServiceClient.getCardById(CARD_ID, TOKEN)).thenReturn(ResponseEntity.ok(new CardResponse()));
        when(cardLabelRepository.existsByCardIdAndLabelId(CARD_ID, LABEL_ID)).thenReturn(false);

        labelService.addLabelToCard(req, TOKEN);

        verify(cardLabelRepository).save(any(CardLabel.class));
    }

    @Test
    void addLabelToCard_alreadyAttached_throwsBadRequest() {
        CardLabelRequest req = new CardLabelRequest();
        req.setLabelId(LABEL_ID);
        req.setCardId(CARD_ID);

        when(labelRepository.findById(LABEL_ID)).thenReturn(Optional.of(sampleLabel));
        when(cardServiceClient.getCardById(CARD_ID, TOKEN)).thenReturn(ResponseEntity.ok(new CardResponse()));
        when(cardLabelRepository.existsByCardIdAndLabelId(CARD_ID, LABEL_ID)).thenReturn(true);

        assertThatThrownBy(() -> labelService.addLabelToCard(req, TOKEN))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already attached");
    }

    // ─── removeLabelFromCard ──────────────────────────────────────────────────

    @Test
    void removeLabelFromCard_happyPath_succeeds() {
        when(cardLabelRepository.existsByCardIdAndLabelId(CARD_ID, LABEL_ID)).thenReturn(true);

        labelService.removeLabelFromCard(CARD_ID, LABEL_ID);

        verify(cardLabelRepository).deleteByCardIdAndLabelId(CARD_ID, LABEL_ID);
    }

    @Test
    void removeLabelFromCard_notAttached_throwsBadRequest() {
        when(cardLabelRepository.existsByCardIdAndLabelId(CARD_ID, LABEL_ID)).thenReturn(false);

        assertThatThrownBy(() -> labelService.removeLabelFromCard(CARD_ID, LABEL_ID))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("not attached");
    }

    // ─── getLabelsForCard ─────────────────────────────────────────────────────

    @Test
    void getLabelsForCard_returnsList() {
        CardLabel cl = new CardLabel();
        cl.setCardId(CARD_ID);
        cl.setLabelId(LABEL_ID);

        when(cardLabelRepository.findByCardId(CARD_ID)).thenReturn(List.of(cl));
        when(labelRepository.findById(LABEL_ID)).thenReturn(Optional.of(sampleLabel));

        List<LabelResponse> results = labelService.getLabelsForCard(CARD_ID);

        assertThat(results).hasSize(1);
    }

    // ─── createChecklist ──────────────────────────────────────────────────────

    @Test
    void createChecklist_happyPath_returnsResponse() {
        CreateChecklistRequest req = new CreateChecklistRequest();
        req.setCardId(CARD_ID);
        req.setTitle("Test Checklist");
        req.setPosition(0);

        when(cardServiceClient.getCardById(CARD_ID, TOKEN)).thenReturn(ResponseEntity.ok(new CardResponse()));
        when(checklistRepository.save(any(Checklist.class))).thenReturn(sampleChecklist);
        when(itemRepository.findByChecklist_ChecklistId(CHECKLIST_ID)).thenReturn(List.of());

        ChecklistResponse result = labelService.createChecklist(req, TOKEN);

        assertThat(result).isNotNull();
        assertThat(result.getChecklistId()).isEqualTo(CHECKLIST_ID);
    }

    // ─── getChecklistsByCard ──────────────────────────────────────────────────

    @Test
    void getChecklistsByCard_returnsList() {
        when(checklistRepository.findByCardIdOrderByPosition(CARD_ID)).thenReturn(List.of(sampleChecklist));
        when(itemRepository.findByChecklist_ChecklistId(CHECKLIST_ID)).thenReturn(List.of());

        List<ChecklistResponse> results = labelService.getChecklistsByCard(CARD_ID);

        assertThat(results).hasSize(1);
    }

    // ─── deleteChecklist ──────────────────────────────────────────────────────

    @Test
    void deleteChecklist_found_succeeds() {
        when(checklistRepository.findById(CHECKLIST_ID)).thenReturn(Optional.of(sampleChecklist));

        labelService.deleteChecklist(CHECKLIST_ID);

        verify(checklistRepository).delete(sampleChecklist);
    }

    // ─── addItem ──────────────────────────────────────────────────────────────

    @Test
    void addItem_happyPath_returnsResponse() {
        AddChecklistItemRequest req = new AddChecklistItemRequest();
        req.setText("Do this");

        when(checklistRepository.findById(CHECKLIST_ID)).thenReturn(Optional.of(sampleChecklist));
        when(itemRepository.save(any(ChecklistItem.class))).thenReturn(sampleItem);

        ChecklistItemResponse result = labelService.addItem(CHECKLIST_ID, req);

        assertThat(result).isNotNull();
        assertThat(result.getItemId()).isEqualTo(ITEM_ID);
    }

    // ─── toggleItem ───────────────────────────────────────────────────────────

    @Test
    void toggleItem_notCompleted_togglesToCompleted() {
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(sampleItem));
        when(itemRepository.save(any(ChecklistItem.class))).thenReturn(sampleItem);

        ChecklistItemResponse result = labelService.toggleItem(ITEM_ID);

        assertThat(result).isNotNull();
        verify(itemRepository).save(sampleItem);
    }

    @Test
    void toggleItem_notFound_throwsResourceNotFound() {
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> labelService.toggleItem(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── deleteItem ───────────────────────────────────────────────────────────

    @Test
    void deleteItem_found_succeeds() {
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(sampleItem));

        labelService.deleteItem(ITEM_ID);

        verify(itemRepository).delete(sampleItem);
    }

    // ─── getChecklistProgress ─────────────────────────────────────────────────

    @Test
    void getChecklistProgress_noItems_returnsZero() {
        when(itemRepository.countByChecklist_ChecklistId(CHECKLIST_ID)).thenReturn(0L);

        int progress = labelService.getChecklistProgress(CHECKLIST_ID);

        assertThat(progress).isZero();
    }

    @Test
    void getChecklistProgress_partialCompletion_returnsPercentage() {
        when(itemRepository.countByChecklist_ChecklistId(CHECKLIST_ID)).thenReturn(4L);
        when(itemRepository.countByChecklist_ChecklistIdAndIsCompletedTrue(CHECKLIST_ID)).thenReturn(2L);

        int progress = labelService.getChecklistProgress(CHECKLIST_ID);

        assertThat(progress).isEqualTo(50);
    }

    @Test
    void getChecklistProgress_allComplete_returns100() {
        when(itemRepository.countByChecklist_ChecklistId(CHECKLIST_ID)).thenReturn(3L);
        when(itemRepository.countByChecklist_ChecklistIdAndIsCompletedTrue(CHECKLIST_ID)).thenReturn(3L);

        int progress = labelService.getChecklistProgress(CHECKLIST_ID);

        assertThat(progress).isEqualTo(100);
    }
}
