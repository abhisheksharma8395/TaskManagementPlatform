package com.taskmanagement.app.boardservice.feign;

import com.taskmanagement.app.boardservice.dto.SendNotificationRequest;
import com.taskmanagement.app.boardservice.dto.UserProfileResponse;
import com.taskmanagement.app.boardservice.dto.WorkspaceResponse;
import com.taskmanagement.app.boardservice.exception.BadRequestException;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BoardServiceClientFallbackTest {

    // ─── WorkspaceServiceClient.Fallback ─────────────────────────────────────

    @Test
    void workspaceFallback_getWorkspaceById_throwsBadRequestException() {
        WorkspaceServiceClient.Fallback fallback = new WorkspaceServiceClient.Fallback();

        assertThatThrownBy(() -> fallback.getWorkspaceById(1L, "Bearer token"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Workspace service");
    }

    // ─── AuthServiceClient.Fallback ───────────────────────────────────────────

    @Test
    void authFallback_getUserById_throwsBadRequestException() {
        AuthServiceClient.Fallback fallback = new AuthServiceClient.Fallback();

        assertThatThrownBy(() -> fallback.getUserById(1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Auth service");
    }

    @Test
    void authFallback_getUserByUsername_throwsBadRequestException() {
        AuthServiceClient.Fallback fallback = new AuthServiceClient.Fallback();

        assertThatThrownBy(() -> fallback.getUserByUsername("john"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Auth service");
    }

    // ─── NotificationServiceClient.Fallback ──────────────────────────────────

    @Test
    void notificationFallback_send_returnsOkWithoutThrowing() {
        NotificationServiceClient.Fallback fallback = new NotificationServiceClient.Fallback();
        SendNotificationRequest req = new SendNotificationRequest();
        req.setRecipientId(1L);

        ResponseEntity<Void> result = fallback.send(req, "Bearer token");

        assertThat(result.getStatusCode().value()).isEqualTo(200);
    }
}
