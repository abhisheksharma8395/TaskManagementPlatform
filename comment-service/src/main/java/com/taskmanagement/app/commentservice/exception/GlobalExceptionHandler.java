package com.taskmanagement.app.commentservice.exception;
import org.springframework.http.HttpStatus; import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError; import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler; import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime; import java.util.HashMap; import java.util.Map;
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String,Object>> nf(ResourceNotFoundException e){
        return b(HttpStatus.NOT_FOUND,e.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String,Object>> ad(AccessDeniedException e){
        return b(HttpStatus.FORBIDDEN,e.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String,Object>> br(BadRequestException e){
        return b(HttpStatus.BAD_REQUEST,e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,Object>> val(MethodArgumentNotValidException ex) {
        Map<String,String> fe=new HashMap<>();
        for(FieldError f:ex.getBindingResult().getFieldErrors()) fe.put(f.getField(),f.getDefaultMessage());
        Map<String,Object> body=new HashMap<>(); body.put("status",400); body.put("error","Validation Failed"); body.put("fieldErrors",fe); body.put("timestamp",LocalDateTime.now().toString());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(Exception.class) public ResponseEntity<Map<String,Object>> gen(Exception e){ return b(HttpStatus.INTERNAL_SERVER_ERROR,e.getMessage()); }
    private ResponseEntity<Map<String,Object>> b(HttpStatus s,String msg){
        Map<String,Object> body=new HashMap<>(); body.put("timestamp",LocalDateTime.now().toString()); body.put("status",s.value()); body.put("error",s.getReasonPhrase()); body.put("message",msg);
        return ResponseEntity.status(s).body(body);
    }
}
