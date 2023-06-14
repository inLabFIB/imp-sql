package edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.exceptions;

public class SQLObjectAlreadyExistsException extends RuntimeException {
    public SQLObjectAlreadyExistsException() {
        super();
    }
    public SQLObjectAlreadyExistsException(String message) {
        super(message);
    }
}
