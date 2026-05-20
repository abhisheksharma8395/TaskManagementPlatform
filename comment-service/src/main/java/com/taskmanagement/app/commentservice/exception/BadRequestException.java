package com.taskmanagement.app.commentservice.exception;
public class BadRequestException extends RuntimeException {
    public BadRequestException(String m){
        super(m);
    }
}
