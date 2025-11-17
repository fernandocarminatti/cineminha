package com.pssa.cineminha.exception;

public class VideoNotFoundException extends RuntimeException {
    public VideoNotFoundException(String message) {
        super(message);
    }
    public VideoNotFoundException(){};
}