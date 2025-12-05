package com.vbote.api.domain.exception;

public class UnauthorizedException extends DomainException{

    public UnauthorizedException() {
        super("Unauthorized access");
    }

    protected UnauthorizedException(String message) {
        super(message);
    }
}
