package com.taskmanagement.app.listservice.exception;
public class AccessDeniedException extends RuntimeException {
    public AccessDeniedException(String message) { super(message); }
}
