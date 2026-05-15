package com.taskmanagement.app.cardservice.service;

import com.taskmanagement.app.cardservice.dto.*;
import com.taskmanagement.app.cardservice.entity.Card;
import com.taskmanagement.app.cardservice.exception.BadRequestException;
import com.taskmanagement.app.cardservice.exception.ResourceNotFoundException;
import com.taskmanagement.app.cardservice.feign.AuthServiceClient;
import com.taskmanagement.app.cardservice.feign.BoardServiceClient;
import com.taskmanagement.app.cardservice.feign.ListServiceClient;
import com.taskmanagement.app.cardservice.repository.CardRepository;
import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CardServiceImplTest {

    @Mock
    private CardRepository cardRepository;
    @Mock
    private BoardServiceClient boardServiceClient;
    @Mock
    private ListServiceClient listServiceClient;
    @Mock
    private AuthServiceClient authServiceClient;
    @Mock
    private NotificationServiceClient notificationServiceClient;
    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private CardServiceImpl cardService;

    private Card sampleCard;
    private ListResponse sampleList;
    private BoardResponse sampleBoard;
    private final Long BOARD_ID = 5L;
    private final Long LIST_ID = 3L;
    private final Long CARD_ID = 1L;
    private final Long REQUESTER_ID = 10L;

    @BeforeEach
    void setUp() {
        sampleCard = new Card();
        sampleCard.setCardId(CARD_ID);
        sampleCard.setListId(LIST_ID);
        sampleCard.setBoardId(BOARD_ID);
        sampleCard.setTitle("Test Card");
        sampleCard.setPosition(0);
        sampleCard.setPriority("MEDIUM");
        sampleCard.setStatus("TO_DO");
        sampleCard.setCreatedById(REQUESTER_ID);

        sampleList = new ListResponse();
        sampleList.setListId(LIST_ID);
        sampleList.setBoardId(BOARD_ID);
        sampleList.setName("Test List");
        sampleList.setArchived(false);

        sampleBoard = new BoardResponse();
        sampleBoard.setBoardId(BOARD_ID);
        sampleBoard.setName("Test Board");
        sampleBoard.setClosed(false);

        when(httpServletRequest.getHeader("Authorization")).thenReturn("Bearer test.token");
    }

    // ─── createCard ───────────────────────────────────────────────────────────

    @Test
    void createCard_happyPath_returnsResponse() {
        CreateCardRequest req = new CreateCardRequest();
        req.setBoardId(BOARD_ID);
        req.setListId(LIST_ID);
        req.setTitle("New Card");

        when(boardServiceClient.getById(BOARD_ID, "Bearer test.token")).thenReturn(ResponseEntity.ok(sampleBoard));
        when(listServiceClient.getById(LIST_ID, "Bearer test.token")).thenReturn(ResponseEntity.ok(sampleList));
        when(cardRepository.findMaxPositionByListId(LIST_ID)).thenReturn(Optional.empty());
        when(cardRepository.save(any(Card.class))).thenReturn(sampleCard);

        CardResponse result = cardService.createCard(req, REQUESTER_ID);

        assertThat(result).isNotNull();
        assertThat(result.getCardId()).isEqualTo(CARD_ID);
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    void createCard_boardNotFound_throwsResourceNotFound() {
        CreateCardRequest req = new CreateCardRequest();
        req.setBoardId(BOARD_ID);
        req.setListId(LIST_ID);
        req.setTitle("New Card");

        when(boardServiceClient.getById(BOARD_ID, "Bearer test.token"))
                .thenThrow(mock(FeignException.NotFound.class));

        assertThatThrownBy(() -> cardService.createCard(req, REQUESTER_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Board not found");
    }

    @Test
    void createCard_listDoesNotBelongToBoard_throwsBadRequest() {
        CreateCardRequest req = new CreateCardRequest();
        req.setBoardId(BOARD_ID);
        req.setListId(LIST_ID);
        req.setTitle("New Card");

        ListResponse wrongList = new ListResponse();
        wrongList.setListId(LIST_ID);
        wrongList.setBoardId(99L); // wrong board

        when(boardServiceClient.getById(BOARD_ID, "Bearer test.token")).thenReturn(ResponseEntity.ok(sampleBoard));
        when(listServiceClient.getById(LIST_ID, "Bearer test.token")).thenReturn(ResponseEntity.ok(wrongList));

        assertThatThrownBy(() -> cardService.createCard(req, REQUESTER_ID))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("does not belong to board");
    }

    // ─── getCardById ──────────────────────────────────────────────────────────

    @Test
    void getCardById_found_returnsResponse() {
        when(cardRepository.findById(CARD_ID)).thenReturn(Optional.of(sampleCard));

        CardResponse result = cardService.getCardById(CARD_ID);

        assertThat(result.getCardId()).isEqualTo(CARD_ID);
    }

    @Test
    void getCardById_notFound_throwsResourceNotFound() {
        when(cardRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.getCardById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── getCardsByList ───────────────────────────────────────────────────────

    @Test
    void getCardsByList_returnsList() {
        when(listServiceClient.getById(LIST_ID, "Bearer test.token")).thenReturn(ResponseEntity.ok(sampleList));
        when(cardRepository.findByListIdAndIsArchived(LIST_ID, false)).thenReturn(List.of(sampleCard));

        List<CardResponse> results = cardService.getCardsByList(LIST_ID);

        assertThat(results).hasSize(1);
    }

    // ─── getCardsByBoard ──────────────────────────────────────────────────────

    @Test
    void getCardsByBoard_returnsList() {
        when(boardServiceClient.getById(BOARD_ID, "Bearer test.token")).thenReturn(ResponseEntity.ok(sampleBoard));
        when(cardRepository.findByBoardIdAndIsArchived(BOARD_ID, false)).thenReturn(List.of(sampleCard));

        List<CardResponse> results = cardService.getCardsByBoard(BOARD_ID);

        assertThat(results).hasSize(1);
    }

    // ─── getCardsByAssignee ───────────────────────────────────────────────────

    @Test
    void getCardsByAssignee_returnsList() {
        sampleCard.setAssigneeId(REQUESTER_ID);
        when(cardRepository.findByAssigneeId(REQUESTER_ID)).thenReturn(List.of(sampleCard));

        List<CardResponse> results = cardService.getCardsByAssignee(REQUESTER_ID);

        assertThat(results).hasSize(1);
    }

    // ─── updateCard ───────────────────────────────────────────────────────────

    @Test
    void updateCard_notArchived_succeeds() {
        UpdateCardRequest req = new UpdateCardRequest();
        req.setTitle("Updated Title");

        when(cardRepository.findById(CARD_ID)).thenReturn(Optional.of(sampleCard));
        when(cardRepository.save(any(Card.class))).thenReturn(sampleCard);

        CardResponse result = cardService.updateCard(CARD_ID, req, REQUESTER_ID);

        assertThat(result).isNotNull();
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    void updateCard_archivedCard_throwsBadRequest() {
        sampleCard.setArchived(true);
        UpdateCardRequest req = new UpdateCardRequest();

        when(cardRepository.findById(CARD_ID)).thenReturn(Optional.of(sampleCard));

        assertThatThrownBy(() -> cardService.updateCard(CARD_ID, req, REQUESTER_ID))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("archived");
    }

    // ─── moveCard ─────────────────────────────────────────────────────────────

    @Test
    void moveCard_happyPath_succeeds() {
        Long targetListId = 99L;
        ListResponse targetList = new ListResponse();
        targetList.setListId(targetListId);
        targetList.setBoardId(BOARD_ID);

        MoveCardRequest req = new MoveCardRequest();
        req.setTargetListId(targetListId);
        req.setPosition(0);

        when(cardRepository.findById(CARD_ID)).thenReturn(Optional.of(sampleCard));
        when(listServiceClient.getById(targetListId, "Bearer test.token")).thenReturn(ResponseEntity.ok(targetList));
        when(cardRepository.save(any(Card.class))).thenReturn(sampleCard);

        CardResponse result = cardService.moveCard(CARD_ID, req);

        assertThat(result).isNotNull();
    }

    // ─── setPriority ──────────────────────────────────────────────────────────

    @Test
    void setPriority_succeeds() {
        SetPriorityRequest req = new SetPriorityRequest();
        req.setPriority("HIGH");

        when(cardRepository.findById(CARD_ID)).thenReturn(Optional.of(sampleCard));
        when(cardRepository.save(any(Card.class))).thenReturn(sampleCard);

        CardResponse result = cardService.setPriority(CARD_ID, req);

        assertThat(result).isNotNull();
        verify(cardRepository).save(sampleCard);
    }

    // ─── setStatus ────────────────────────────────────────────────────────────

    @Test
    void setStatus_succeeds() {
        SetStatusRequest req = new SetStatusRequest();
        req.setStatus("IN_PROGRESS");

        when(cardRepository.findById(CARD_ID)).thenReturn(Optional.of(sampleCard));
        when(cardRepository.save(any(Card.class))).thenReturn(sampleCard);

        CardResponse result = cardService.setStatus(CARD_ID, req);

        assertThat(result).isNotNull();
    }

    // ─── archiveCard ──────────────────────────────────────────────────────────

    @Test
    void archiveCard_succeeds() {
        when(cardRepository.findById(CARD_ID)).thenReturn(Optional.of(sampleCard));
        when(cardRepository.save(any(Card.class))).thenReturn(sampleCard);

        CardResponse result = cardService.archiveCard(CARD_ID);

        assertThat(result).isNotNull();
        verify(cardRepository).save(sampleCard);
    }

    // ─── unarchiveCard ────────────────────────────────────────────────────────

    @Test
    void unarchiveCard_succeeds() {
        sampleCard.setArchived(true);
        when(cardRepository.findById(CARD_ID)).thenReturn(Optional.of(sampleCard));
        when(cardRepository.save(any(Card.class))).thenReturn(sampleCard);

        CardResponse result = cardService.unarchiveCard(CARD_ID);

        assertThat(result).isNotNull();
    }

    // ─── deleteCard ───────────────────────────────────────────────────────────

    @Test
    void deleteCard_archivedCard_succeeds() {
        sampleCard.setArchived(true);
        when(cardRepository.findById(CARD_ID)).thenReturn(Optional.of(sampleCard));

        cardService.deleteCard(CARD_ID);

        verify(cardRepository).delete(sampleCard);
    }

    @Test
    void deleteCard_notArchived_throwsBadRequest() {
        when(cardRepository.findById(CARD_ID)).thenReturn(Optional.of(sampleCard));

        assertThatThrownBy(() -> cardService.deleteCard(CARD_ID))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("archived");
    }

    // ─── getOverdueCards ──────────────────────────────────────────────────────

    @Test
    void getOverdueCards_returnsList() {
        when(cardRepository.findOverdueCards(any(LocalDate.class))).thenReturn(List.of(sampleCard));

        List<CardResponse> results = cardService.getOverdueCards();

        assertThat(results).hasSize(1);
    }

    // ─── getOverdueCardsByBoard ───────────────────────────────────────────────

    @Test
    void getOverdueCardsByBoard_returnsList() {
        when(boardServiceClient.getById(BOARD_ID, "Bearer test.token")).thenReturn(ResponseEntity.ok(sampleBoard));
        when(cardRepository.findOverdueCardsByBoard(eq(BOARD_ID), any(LocalDate.class)))
                .thenReturn(List.of(sampleCard));

        List<CardResponse> results = cardService.getOverdueCardsByBoard(BOARD_ID);

        assertThat(results).hasSize(1);
    }
}
