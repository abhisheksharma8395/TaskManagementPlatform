package com.taskmanagement.app.cardservice.exception;
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) { super(message); }
}
