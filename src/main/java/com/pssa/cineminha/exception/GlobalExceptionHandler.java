package com.pssa.cineminha.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.IOException;
import java.util.Objects;

@ControllerAdvice
public class GlobalExceptionHandler {
    final String SERVER_EXPLODED = "Could not handle request, server exploded.";

    @ExceptionHandler(RemuxProcessingException.class)
    public ResponseEntity<String> handleRemuxProcessingException(RemuxProcessingException e) {
        return ResponseEntity.internalServerError().body(e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = Objects.requireNonNull(e.getBindingResult().getFieldError()).getDefaultMessage();
        return ResponseEntity.badRequest().body(message);
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<String> handleIOException(IOException e) {
        return ResponseEntity.internalServerError().body(SERVER_EXPLODED);
    }

    @ExceptionHandler(InterruptedException.class)
    public ResponseEntity<String> handleInterruptedException(InterruptedException e) {
        return ResponseEntity.internalServerError().body(SERVER_EXPLODED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        return ResponseEntity.internalServerError().body(SERVER_EXPLODED);
    }
}