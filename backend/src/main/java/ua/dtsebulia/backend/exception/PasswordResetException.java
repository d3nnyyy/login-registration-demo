package ua.dtsebulia.backend.exception;

public class PasswordResetException extends RuntimeException{
    public PasswordResetException(String message) {
        super(message);
    }
}
