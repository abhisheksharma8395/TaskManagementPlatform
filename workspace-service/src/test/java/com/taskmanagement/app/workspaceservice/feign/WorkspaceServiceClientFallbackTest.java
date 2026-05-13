package com.taskmanagement.app.workspaceservice.feign;

import com.taskmanagement.app.workspaceservice.exception.WorkspaceOperationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WorkspaceServiceClientFallbackTest {

    @Test
    void authFallback_getUserById_throwsWorkspaceOperationException() {
        AuthServiceClient.Fallback fallback = new AuthServiceClient.Fallback();

        assertThatThrownBy(() -> fallback.getUserById(1L))
                .isInstanceOf(WorkspaceOperationException.class)
                .hasMessageContaining("Auth service");
    }

    @Test
    void authFallback_getUserByUsername_throwsWorkspaceOperationException() {
        AuthServiceClient.Fallback fallback = new AuthServiceClient.Fallback();

        assertThatThrownBy(() -> fallback.getUserByUsername("john"))
                .isInstanceOf(WorkspaceOperationException.class)
                .hasMessageContaining("Auth service");
    }
}
