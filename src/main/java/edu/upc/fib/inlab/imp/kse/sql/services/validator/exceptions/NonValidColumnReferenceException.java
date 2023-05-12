package edu.upc.fib.inlab.imp.kse.sql.services.validator.exceptions;

public class NonValidColumnReferenceException extends RuntimeException {

    public NonValidColumnReferenceException() {
        super();
    }
    public NonValidColumnReferenceException(String message) {
        super(message);
    }
}
