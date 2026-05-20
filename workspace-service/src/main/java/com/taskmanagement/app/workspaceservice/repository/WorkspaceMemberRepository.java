package com.taskmanagement.app.workspaceservice.repository;

import com.taskmanagement.app.workspaceservice.entity.WorkspaceMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, Long> {

    List<WorkspaceMember> findByWorkspace_WorkspaceId(Long workspaceId);

    Optional<WorkspaceMember> findByWorkspace_WorkspaceIdAndUserId(Long workspaceId, Long userId);

    boolean existsByWorkspace_WorkspaceIdAndUserId(Long workspaceId, Long userId);

    List<WorkspaceMember> findByUserId(Long userId);

    void deleteByWorkspace_WorkspaceIdAndUserId(Long workspaceId, Long userId);
}
