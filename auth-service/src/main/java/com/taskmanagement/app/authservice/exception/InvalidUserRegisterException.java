package com.taskmanagement.app.authservice.exception;

public class InvalidUserRegisterException extends Exception{
    public InvalidUserRegisterException(String message){
        super(message);
    }
}
