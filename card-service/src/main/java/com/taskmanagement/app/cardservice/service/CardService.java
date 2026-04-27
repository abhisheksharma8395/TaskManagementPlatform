package com.taskmanagement.app.cardservice.service;

import com.taskmanagement.app.cardservice.dto.*;

import java.util.List;

public interface CardService {

    CardResponse createCard(CreateCardRequest request, Long requesterId);

    CardResponse getCardById(Long cardId);

    List<CardResponse> getCardsByList(Long listId);

    List<CardResponse> getCardsByBoard(Long boardId);

    List<CardResponse> getCardsByAssignee(Long assigneeId);

    List<CardResponse> getArchivedCards(Long boardId);

    CardResponse updateCard(Long cardId, UpdateCardRequest request, Long requesterId);

    CardResponse moveCard(Long cardId, MoveCardRequest request);

    void reorderCards(Long listId, ReorderCardsRequest request);

    CardResponse setAssignee(Long cardId, SetAssigneeRequest request);

    CardResponse setPriority(Long cardId, SetPriorityRequest request);

    CardResponse setStatus(Long cardId, SetStatusRequest request);

    CardResponse archiveCard(Long cardId);

    CardResponse unarchiveCard(Long cardId);

    void deleteCard(Long cardId);

    List<CardResponse> getOverdueCards();

    List<CardResponse> getOverdueCardsByBoard(Long boardId);
}
