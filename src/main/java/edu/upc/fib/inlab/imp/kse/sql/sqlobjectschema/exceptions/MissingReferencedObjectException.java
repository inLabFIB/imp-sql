package edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.exceptions;

public class MissingReferencedObjectException extends RuntimeException {
    public MissingReferencedObjectException() {
        super();
    }
    public MissingReferencedObjectException(String message) {
        super(message);
    }
}
