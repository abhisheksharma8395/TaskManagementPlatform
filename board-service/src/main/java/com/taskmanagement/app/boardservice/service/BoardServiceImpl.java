package com.taskmanagement.app.boardservice.service;

import com.taskmanagement.app.boardservice.dto.*;
import com.taskmanagement.app.boardservice.entity.Board;
import com.taskmanagement.app.boardservice.entity.BoardMember;
import com.taskmanagement.app.boardservice.exception.AccessDeniedException;
import com.taskmanagement.app.boardservice.exception.BadRequestException;
import com.taskmanagement.app.boardservice.exception.ResourceNotFoundException;
import com.taskmanagement.app.boardservice.feign.AuthServiceClient;
import com.taskmanagement.app.boardservice.feign.WorkspaceServiceClient;
import com.taskmanagement.app.boardservice.messaging.NotificationPublisher;
import com.taskmanagement.app.boardservice.repository.BoardMemberRepository;
import com.taskmanagement.app.boardservice.repository.BoardRepository;
import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BoardServiceImpl implements BoardService {

    @Autowired private BoardRepository boardRepository;
    @Autowired private BoardMemberRepository memberRepository;
    @Autowired private WorkspaceServiceClient workspaceServiceClient;
    @Autowired private HttpServletRequest httpServletRequest;
    @Autowired private AuthServiceClient authServiceClient;
    @Autowired private NotificationPublisher notificationPublisher;

    @Override
    @Transactional
    public BoardResponse createBoard(CreateBoardRequest request, Long requesterId) {

        String token = httpServletRequest.getHeader("Authorization");
        try {
            workspaceServiceClient.getWorkspaceById(request.getWorkspaceId(), token);
        } catch (FeignException.NotFound e) {
            throw new BadRequestException("Workspace not found with id: " + request.getWorkspaceId());
        } catch (FeignException e) {
            throw new BadRequestException("Could not verify workspace: " + e.getMessage());
        } catch (RuntimeException e) {
            throw new BadRequestException("Workspace service is unavailable. Please try again later.");
        }

        Board board = new Board();
        board.setWorkspaceId(request.getWorkspaceId());
        board.setName(request.getName());
        board.setDescription(request.getDescription());
        board.setBackground(request.getBackground());
        board.setVisibility(request.getVisibility() != null ? request.getVisibility() : "PUBLIC");
        board.setCreatedById(requesterId);
        board = boardRepository.save(board);

        // Auto-add creator as ADMIN board member
        BoardMember owner = new BoardMember();
        owner.setBoard(board);
        owner.setUserId(requesterId);
        owner.setRole("ADMIN");
        memberRepository.save(owner);

        return toResponse(board);
    }

    @Override
    public BoardResponse getBoardById(Long boardId, Long requesterId) {
        Board board = findOrThrow(boardId);
        assertCanView(board, requesterId);
        return toResponse(board);
    }

    @Override
    public List<BoardResponse> getBoardsByWorkspace(Long workspaceId, Long requesterId) {
        return boardRepository.findByWorkspaceId(workspaceId).stream()
                .filter(b -> canView(b, requesterId))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<BoardResponse> getBoardsByMember(Long userId) {
        return boardRepository.findByMemberUserId(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BoardResponse updateBoard(Long boardId, UpdateBoardRequest request, Long requesterId) {
        Board board = findOrThrow(boardId);
        assertAdminOrCreator(board, requesterId);
        if (board.isClosed()) throw new BadRequestException("Cannot update a closed board");

        if (request.getName() != null && !request.getName().isBlank()) board.setName(request.getName());
        if (request.getDescription() != null) board.setDescription(request.getDescription());
        if (request.getBackground() != null) board.setBackground(request.getBackground());
        if (request.getVisibility() != null) board.setVisibility(request.getVisibility());

        return toResponse(boardRepository.save(board));
    }

    @Override
    @Transactional
    public BoardResponse closeBoard(Long boardId, Long requesterId) {
        Board board = findOrThrow(boardId);
        assertAdminOrCreator(board, requesterId);
        board.setClosed(true);
        return toResponse(boardRepository.save(board));
    }

    @Override
    @Transactional
    public void deleteBoard(Long boardId, Long requesterId) {
        Board board = findOrThrow(boardId);
        if (!board.getCreatedById().equals(requesterId))
            throw new AccessDeniedException("Only the board creator can delete this board");
        boardRepository.delete(board);
    }

    @Override
    @Transactional
    public BoardMemberResponse addMember(Long boardId, AddBoardMemberRequest request, Long requesterId) {
        Board board = findOrThrow(boardId);
        assertAdminOrCreator(board, requesterId);
        if (memberRepository.existsByBoard_BoardIdAndUserId(boardId, request.getUserId()))
            throw new BadRequestException("User " + request.getUserId() + " is already a member of this board");

        BoardMember member = new BoardMember();
        member.setBoard(board);
        member.setUserId(request.getUserId());
        member.setRole(request.getRole() != null ? request.getRole() : "MEMBER");
        BoardMemberResponse saved = toMemberResponse(memberRepository.save(member));

        // Publish notification to RabbitMQ
        UserProfileResponse newMember = authServiceClient.getUserById(request.getUserId());
        NotificationEvent event = new NotificationEvent();
        event.setRecipientId(request.getUserId());
        event.setRecipientEmail(newMember != null ? newMember.getEmail() : null);
        event.setActorId(requesterId);
        event.setType("ASSIGNMENT");
        event.setTitle("You were added to a board");
        event.setMessage("You have been added to board: " + board.getName() + " as " + member.getRole());
        event.setRelatedId(boardId);
        event.setRelatedType("BOARD");
        event.setDeepLinkUrl("/boards/" + boardId);
        notificationPublisher.publish(event);

        return saved;
    }

    @Override
    @Transactional
    public void removeMember(Long boardId, Long userId, Long requesterId) {
        Board board = findOrThrow(boardId);
        assertAdminOrCreator(board, requesterId);
        if (board.getCreatedById().equals(userId))
            throw new BadRequestException("Cannot remove the board creator");
        if (!memberRepository.existsByBoard_BoardIdAndUserId(boardId, userId))
            throw new BadRequestException("User " + userId + " is not a member of this board");
        memberRepository.deleteByBoard_BoardIdAndUserId(boardId, userId);
    }

    @Override
    public List<BoardResponse> getAllBoards() {
        return boardRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BoardMemberResponse updateMemberRole(Long boardId, Long userId,
                                                UpdateBoardMemberRoleRequest request, Long requesterId) {
        Board board = findOrThrow(boardId);
        assertAdminOrCreator(board, requesterId);
        BoardMember member = memberRepository.findByBoard_BoardIdAndUserId(boardId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));
        member.setRole(request.getRole());
        return toMemberResponse(memberRepository.save(member));
    }

    @Override
    public List<BoardMemberResponse> getMembers(Long boardId, Long requesterId) {
        Board board = findOrThrow(boardId);
        assertCanView(board, requesterId);
        return memberRepository.findByBoard_BoardId(boardId).stream()
                .map(this::toMemberResponse)
                .collect(Collectors.toList());
    }

    private Board findOrThrow(Long boardId) {
        return boardRepository.findById(boardId).orElseThrow(() -> new ResourceNotFoundException("Board not found with id: " + boardId));
    }

    private boolean canView(Board b, Long requesterId) {
        if ("PUBLIC".equalsIgnoreCase(b.getVisibility())) return true;
        if (b.getCreatedById().equals(requesterId)) return true;
        return memberRepository.existsByBoard_BoardIdAndUserId(b.getBoardId(), requesterId);
    }

    private void assertCanView(Board b, Long requesterId) {
        if (!canView(b, requesterId)) throw new AccessDeniedException("You do not have access to this board");
    }

    private void assertAdminOrCreator(Board b, Long requesterId) {
        if (b.getCreatedById().equals(requesterId)) return;
        memberRepository.findByBoard_BoardIdAndUserId(b.getBoardId(), requesterId)
                .filter(m -> "ADMIN".equalsIgnoreCase(m.getRole()))
                .orElseThrow(() -> new AccessDeniedException("Only board admins or the creator can perform this action"));
    }

    private BoardResponse toResponse(Board b) {
        BoardResponse r = new BoardResponse();
        r.setBoardId(b.getBoardId());
        r.setWorkspaceId(b.getWorkspaceId());
        r.setName(b.getName());
        r.setDescription(b.getDescription());
        r.setBackground(b.getBackground());
        r.setVisibility(b.getVisibility());
        r.setCreatedById(b.getCreatedById());
        r.setClosed(b.isClosed());
        r.setMemberCount(b.getMembers() != null ? b.getMembers().size() : 0);
        r.setCreatedAt(b.getCreatedAt());
        r.setUpdatedAt(b.getUpdatedAt());
        return r;
    }

    private BoardMemberResponse toMemberResponse(BoardMember m) {
        BoardMemberResponse r = new BoardMemberResponse();
        r.setBoardMemberId(m.getBoardMemberId());
        r.setBoardId(m.getBoard().getBoardId());
        r.setUserId(m.getUserId());
        r.setRole(m.getRole());
        r.setAddedAt(m.getAddedAt());
        return r;
    }
}