package com.taskmanagement.app.boardservice.service;

import com.taskmanagement.app.boardservice.dto.*;

import java.util.List;

public interface BoardService {

    BoardResponse createBoard(CreateBoardRequest request, Long requesterId);

    BoardResponse getBoardById(Long boardId, Long requesterId);

    List<BoardResponse> getBoardsByWorkspace(Long workspaceId, Long requesterId);

    List<BoardResponse> getBoardsByMember(Long userId);

    BoardResponse updateBoard(Long boardId, UpdateBoardRequest request, Long requesterId);

    BoardResponse closeBoard(Long boardId, Long requesterId);

    void deleteBoard(Long boardId, Long requesterId);

    BoardMemberResponse addMember(Long boardId, AddBoardMemberRequest request, Long requesterId);

    void removeMember(Long boardId, Long userId, Long requesterId);

    List<BoardResponse> getAllBoards();

    BoardMemberResponse updateMemberRole(Long boardId, Long userId, UpdateBoardMemberRoleRequest request, Long requesterId);

    List<BoardMemberResponse> getMembers(Long boardId, Long requesterId);
}
