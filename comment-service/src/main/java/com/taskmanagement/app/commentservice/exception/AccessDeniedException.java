package com.taskmanagement.app.commentservice.exception;
public class AccessDeniedException extends RuntimeException {
    public AccessDeniedException(String m){
        super(m);
    }
}
