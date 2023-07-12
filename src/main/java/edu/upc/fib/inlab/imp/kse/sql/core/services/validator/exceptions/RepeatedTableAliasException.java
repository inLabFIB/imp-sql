package edu.upc.fib.inlab.imp.kse.sql.core.services.validator.exceptions;

public class RepeatedTableAliasException extends RuntimeException {

    public RepeatedTableAliasException() {
        super();
    }
    public RepeatedTableAliasException(String message) {
        super(message);
    }
}
