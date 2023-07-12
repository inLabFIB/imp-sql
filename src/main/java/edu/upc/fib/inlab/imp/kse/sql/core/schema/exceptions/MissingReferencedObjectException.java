package edu.upc.fib.inlab.imp.kse.sql.core.schema.exceptions;

public class MissingReferencedObjectException extends RuntimeException {
    public MissingReferencedObjectException() {
        super();
    }
    public MissingReferencedObjectException(String message) {
        super(message);
    }
}
