package de.edvschuleplattling.irgendwieanders.Exceptions;

public class ExpiredIdException extends RuntimeException {
    public ExpiredIdException(String message) {
        super(message);
    }
}
