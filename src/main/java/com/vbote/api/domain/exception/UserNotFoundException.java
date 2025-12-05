package com.vbote.api.domain.exception;

public class UserNotFoundException extends DomainException{

    public UserNotFoundException(Long id) {
        super("User not found with id: " + id);
    }

    public UserNotFoundException(String username) {
        super("User not found with username: " + username);
    }
}
