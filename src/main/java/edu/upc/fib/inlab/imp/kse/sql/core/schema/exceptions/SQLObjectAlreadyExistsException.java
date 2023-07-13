package edu.upc.fib.inlab.imp.kse.sql.core.schema.exceptions;

public class SQLObjectAlreadyExistsException extends RuntimeException {
    public SQLObjectAlreadyExistsException() {
        super();
    }
    public SQLObjectAlreadyExistsException(String message) {
        super(message);
    }
}
