package com.taskmanagement.app.workspaceservice.repository;

import com.taskmanagement.app.workspaceservice.entity.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {

    List<Workspace> findByOwnerId(Long ownerId);

    Optional<Workspace> findByWorkspaceId(Long workspaceId);

    @Query("SELECT w FROM Workspace w JOIN w.members m WHERE m.userId = :userId")
    List<Workspace> findByMemberUserId(@Param("userId") Long userId);

    List<Workspace> findByVisibility(String visibility);

    boolean existsByNameAndOwnerId(String name, Long ownerId);

    long countByOwnerId(Long ownerId);
}
