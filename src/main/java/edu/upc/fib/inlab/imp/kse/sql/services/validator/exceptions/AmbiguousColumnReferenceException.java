package edu.upc.fib.inlab.imp.kse.sql.services.validator.exceptions;

public class AmbiguousColumnReferenceException extends RuntimeException {

    public AmbiguousColumnReferenceException() {
        super();
    }
    public AmbiguousColumnReferenceException(String message) {
        super(message);
    }
}
