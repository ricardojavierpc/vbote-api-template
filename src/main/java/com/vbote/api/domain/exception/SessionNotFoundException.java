package com.vbote.api.domain.exception;

public class SessionNotFoundException extends DomainException{

    public SessionNotFoundException(Long id) {
        super("Session not found with id: " + id);
    }

    public SessionNotFoundException(String token) {
        super("Session not found with token");
    }
}
