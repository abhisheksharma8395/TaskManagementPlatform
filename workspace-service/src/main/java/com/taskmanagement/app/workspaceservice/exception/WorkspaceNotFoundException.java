package com.taskmanagement.app.workspaceservice.exception;

public class WorkspaceNotFoundException extends RuntimeException {
    public WorkspaceNotFoundException(String message) {
        super(message);
    }
}
