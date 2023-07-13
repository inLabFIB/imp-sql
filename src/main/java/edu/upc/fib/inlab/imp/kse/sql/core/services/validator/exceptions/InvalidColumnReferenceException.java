package edu.upc.fib.inlab.imp.kse.sql.core.services.validator.exceptions;

public class InvalidColumnReferenceException extends RuntimeException {

    public InvalidColumnReferenceException() {
        super();
    }
    public InvalidColumnReferenceException(String message) {
        super(message);
    }
}
