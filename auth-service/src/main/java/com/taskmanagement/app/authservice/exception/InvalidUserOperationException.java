package com.taskmanagement.app.authservice.exception;

public class InvalidUserOperationException extends Exception{
    public InvalidUserOperationException(String message){
        super(message);
    }
}
