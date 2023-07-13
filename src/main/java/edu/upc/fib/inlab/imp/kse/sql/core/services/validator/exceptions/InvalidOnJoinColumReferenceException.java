package edu.upc.fib.inlab.imp.kse.sql.core.services.validator.exceptions;

public class InvalidOnJoinColumReferenceException extends RuntimeException {

    public InvalidOnJoinColumReferenceException() {
        super();
    }
    public InvalidOnJoinColumReferenceException(String message) {
        super(message);
    }
}
