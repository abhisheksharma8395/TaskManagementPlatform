package com.taskmanagement.app.cardservice.service;

import com.taskmanagement.app.cardservice.dto.*;
import com.taskmanagement.app.cardservice.entity.Card;
import com.taskmanagement.app.cardservice.exception.BadRequestException;
import com.taskmanagement.app.cardservice.exception.ResourceNotFoundException;
import com.taskmanagement.app.cardservice.feign.AuthServiceClient;
import com.taskmanagement.app.cardservice.feign.BoardServiceClient;
import com.taskmanagement.app.cardservice.feign.ListServiceClient;
import com.taskmanagement.app.cardservice.feign.NotificationServiceClient;
import com.taskmanagement.app.cardservice.repository.CardRepository;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CardServiceImpl implements CardService {

    @Autowired
    private CardRepository cardRepository;

    @Autowired(required = false)
    private NotificationServiceClient notificationServiceClient;

    @Autowired
    private BoardServiceClient boardServiceClient;

    @Autowired
    private AuthServiceClient authServiceClient;

    @Autowired
    private ListServiceClient listServiceClient;

    @Autowired
    private HttpServletRequest httpServletRequest;


    @Override
    @Transactional
    public CardResponse createCard(CreateCardRequest request, Long requesterId) {
        // Validate board exists
        validateBoard(request.getBoardId());

        // Validate list exists and belongs to the specified board
        ListResponse list = validateList(request.getListId());
        if (!list.getBoardId().equals(request.getBoardId())) {
            throw new BadRequestException(
                    "List " + request.getListId() + " does not belong to board " + request.getBoardId());
        }

        int nextPosition = cardRepository.findMaxPositionByListId(request.getListId())
                .map(max -> max + 1).orElse(0);

        Card card = new Card();
        card.setListId(request.getListId());
        card.setBoardId(request.getBoardId());
        card.setTitle(request.getTitle());
        card.setDescription(request.getDescription());
        card.setPosition(nextPosition);
        card.setPriority(request.getPriority() != null ? request.getPriority() : "MEDIUM");
        card.setStatus("TO_DO");
        card.setDueDate(request.getDueDate());
        card.setStartDate(request.getStartDate());
        card.setAssigneeId(request.getAssigneeId());
        card.setCreatedById(requesterId);
        card.setCoverColor(request.getCoverColor());

        return toResponse(cardRepository.save(card));
    }

    @Override
    public CardResponse getCardById(Long cardId) {
        return toResponse(findOrThrow(cardId));
    }

    @Override
    public List<CardResponse> getCardsByList(Long listId) {
        // Validate list exists before querying cards
        validateList(listId);
        return cardRepository.findByListIdAndIsArchived(listId, false)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<CardResponse> getCardsByBoard(Long boardId) {
        // Validate board exists before querying cards
        validateBoard(boardId);
        return cardRepository.findByBoardIdAndIsArchived(boardId, false)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<CardResponse> getCardsByAssignee(Long assigneeId) {
        return cardRepository.findByAssigneeId(assigneeId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<CardResponse> getArchivedCards(Long boardId) {
        // Validate board exists before querying archived cards
        validateBoard(boardId);
        return cardRepository.findByBoardIdAndIsArchived(boardId, true)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CardResponse updateCard(Long cardId, UpdateCardRequest request, Long requesterId) {
        Card card = findOrThrow(cardId);
        if (card.isArchived()) throw new BadRequestException("Cannot update an archived card");

        if (request.getTitle() != null && !request.getTitle().isBlank())
            card.setTitle(request.getTitle());
        if (request.getDescription() != null) card.setDescription(request.getDescription());
        if (request.getPriority() != null)    card.setPriority(request.getPriority());
        if (request.getStatus() != null)      card.setStatus(request.getStatus());
        if (request.getDueDate() != null)     card.setDueDate(request.getDueDate());
        if (request.getStartDate() != null)   card.setStartDate(request.getStartDate());
        if (request.getAssigneeId() != null)  card.setAssigneeId(request.getAssigneeId());
        if (request.getCoverColor() != null)  card.setCoverColor(request.getCoverColor());

        return toResponse(cardRepository.save(card));
    }


    @Override
    @Transactional
    public CardResponse moveCard(Long cardId, MoveCardRequest request) {
        Card card = findOrThrow(cardId);

        // Validate target list exists and update boardId accordingly
        ListResponse targetList = validateList(request.getTargetListId());
        card.setListId(request.getTargetListId());
        card.setBoardId(targetList.getBoardId());   // keep boardId in sync if moving across boards

        if (request.getPosition() != null) {
            card.setPosition(request.getPosition());
        } else {
            int nextPos = cardRepository.findMaxPositionByListId(request.getTargetListId())
                    .map(m -> m + 1).orElse(0);
            card.setPosition(nextPos);
        }
        return toResponse(cardRepository.save(card));
    }

    @Override
    @Transactional
    public void reorderCards(Long listId, ReorderCardsRequest request) {
        // Validate list exists before reordering
        validateList(listId);

        List<Long> ids = request.getOrderedCardIds();
        for (int i = 0; i < ids.size(); i++) {
            Card card = findOrThrow(ids.get(i));
            if (!card.getListId().equals(listId))
                throw new BadRequestException("Card " + ids.get(i) + " does not belong to list " + listId);
            card.setPosition(i);
            cardRepository.save(card);
        }
    }

    @Override
    @Transactional
    public CardResponse setAssignee(Long cardId, SetAssigneeRequest request) {
        Card card = findOrThrow(cardId);
        Long previousAssigneeId = card.getAssigneeId();
        card.setAssigneeId(request.getAssigneeId());
        CardResponse saved = toResponse(cardRepository.save(card));

        if (notificationServiceClient != null
                && request.getAssigneeId() != null
                && !request.getAssigneeId().equals(previousAssigneeId)) {
            try {
                String token = getToken();
                UserProfileResponse assignee = authServiceClient.getUserById(request.getAssigneeId());
                if (assignee != null) {
                    SendNotificationRequest notif = new SendNotificationRequest();
                    notif.setRecipientId(assignee.getUserId());
                    notif.setRecipientEmail(assignee.getEmail());
                    notif.setActorId(null);
                    notif.setType("ASSIGNMENT");
                    notif.setTitle("You have been assigned a card");
                    notif.setMessage("You have been assigned to card: " + card.getTitle());
                    notif.setRelatedId(cardId);
                    notif.setRelatedType("CARD");
                    notif.setDeepLinkUrl("/cards/" + cardId);
                    notificationServiceClient.send(notif, token);
                }
            } catch (Exception e) {
                System.err.println("[CardService] Failed to send assignment notification: " + e.getMessage());
            }
        }
        return saved;
    }

    @Override
    @Transactional
    public CardResponse setPriority(Long cardId, SetPriorityRequest request) {
        Card card = findOrThrow(cardId);
        card.setPriority(request.getPriority());
        return toResponse(cardRepository.save(card));
    }

    @Override
    @Transactional
    public CardResponse setStatus(Long cardId, SetStatusRequest request) {
        Card card = findOrThrow(cardId);
        card.setStatus(request.getStatus());
        CardResponse saved = toResponse(cardRepository.save(card));

        if (notificationServiceClient != null
                && "DONE".equalsIgnoreCase(request.getStatus())
                && card.getAssigneeId() != null) {
            try {
                String token = getToken();
                UserProfileResponse assignee = authServiceClient.getUserById(card.getAssigneeId());
                if (assignee != null) {
                    SendNotificationRequest notif = new SendNotificationRequest();
                    notif.setRecipientId(assignee.getUserId());
                    notif.setRecipientEmail(null);
                    notif.setActorId(null);
                    notif.setType("MOVE");
                    notif.setTitle("Card moved to Done");
                    notif.setMessage("Card \"" + card.getTitle() + "\" has been moved to Done.");
                    notif.setRelatedId(card.getCardId());
                    notif.setRelatedType("CARD");
                    notif.setDeepLinkUrl("/cards/" + card.getCardId());
                    notificationServiceClient.send(notif, token);
                }
            } catch (Exception e) {
                System.err.println("[CardService] Failed to send status notification: " + e.getMessage());
            }
        }
        return saved;
    }


    @Override
    @Transactional
    public CardResponse archiveCard(Long cardId) {
        Card card = findOrThrow(cardId);
        card.setArchived(true);
        return toResponse(cardRepository.save(card));
    }

    @Override
    @Transactional
    public CardResponse unarchiveCard(Long cardId) {
        Card card = findOrThrow(cardId);
        card.setArchived(false);
        return toResponse(cardRepository.save(card));
    }

    @Override
    @Transactional
    public void deleteCard(Long cardId) {
        Card card = findOrThrow(cardId);
        if (!card.isArchived())
            throw new BadRequestException("Only archived cards can be permanently deleted");
        cardRepository.delete(card);
    }

    // ── Overdue ───────────────────────────────────────────────────────────────

    @Override
    public List<CardResponse> getOverdueCards() {
        return cardRepository.findOverdueCards(LocalDate.now())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<CardResponse> getOverdueCardsByBoard(Long boardId) {
        // Validate board exists before querying overdue cards
        validateBoard(boardId);
        return cardRepository.findOverdueCardsByBoard(boardId, LocalDate.now())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Card findOrThrow(Long cardId) {
        return cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found with id: " + cardId));
    }

    private String getToken() {
        String header = httpServletRequest.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new RuntimeException("Authorization header missing or malformed");
        }
        return header;
    }

    private void validateBoard(Long boardId) {
        try {
            BoardResponse board = boardServiceClient.getById(boardId, getToken()).getBody();
            if (board == null) {
                throw new ResourceNotFoundException("Board not found with id: " + boardId);
            }
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("Board not found with id: " + boardId);
        }
    }

    private ListResponse validateList(Long listId) {
        try {
            ListResponse list = listServiceClient.getById(listId, getToken()).getBody();
            if (list == null) {
                throw new ResourceNotFoundException("List not found with id: " + listId);
            }
            return list;
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("List not found with id: " + listId);
        }
    }

    private boolean isOverdue(Card c) {
        return c.getDueDate() != null
                && LocalDate.now().isAfter(c.getDueDate())
                && !"DONE".equalsIgnoreCase(c.getStatus());
    }

    private CardResponse toResponse(Card c) {
        CardResponse r = new CardResponse();
        r.setCardId(c.getCardId());
        r.setListId(c.getListId());
        r.setBoardId(c.getBoardId());
        r.setTitle(c.getTitle());
        r.setDescription(c.getDescription());
        r.setPosition(c.getPosition());
        r.setPriority(c.getPriority());
        r.setStatus(c.getStatus());
        r.setDueDate(c.getDueDate());
        r.setStartDate(c.getStartDate());
        r.setAssigneeId(c.getAssigneeId());
        r.setCreatedById(c.getCreatedById());
        r.setArchived(c.isArchived());
        r.setOverdue(isOverdue(c));
        r.setCoverColor(c.getCoverColor());
        r.setCreatedAt(c.getCreatedAt());
        r.setUpdatedAt(c.getUpdatedAt());
        return r;
    }
}
