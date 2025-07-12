package com.example.mauro.devices_api.exception;

public class ResourceCannotBeDeletedException extends RuntimeException {
    public ResourceCannotBeDeletedException(String message) {
        super(message);
    }
}
