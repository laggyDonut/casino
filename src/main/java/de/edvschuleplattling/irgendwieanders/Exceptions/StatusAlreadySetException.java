package de.edvschuleplattling.irgendwieanders.Exceptions;

public class StatusAlreadySetException extends RuntimeException {
    public StatusAlreadySetException(String message) {
        super(message);
    }
}
